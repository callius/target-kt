# Target

Target is a library for Functional Domain Modeling in Kotlin, inspired by [arrow-kt](https://arrow-kt.io).

Target aims to provide a set of tools across all Kotlin platforms to empower users to quickly write pure, functionally
validated domain models. For this, it includes a set of atomic components: `ValueFailure`, `ValueObject`,
and `ValueValidator`. These components can be used on their own, or in conjunction with the
included [KSP](https://kotlinlang.org/docs/ksp-overview.html) annotation processor.

## Getting Started

#### Value Failure

A `ValueFailure` is an interface representing a failure during value validation.

```kotlin
interface ValueFailure<T> {
    val failedValue: T
}
```

#### Value Object

A `ValueObject` is an interface representing a validated value. By convention, value object implementations have a
private primary constructor, so that they are not instantiated outside a `ValueValidator`. A value object implementation
must declare a companion object implementing a value validator when used in conjunction with the annotation processor
library.

```kotlin
interface ValueObject<T> {
    val value: T
}
```

#### Value Validator

A `ValueValidator` is an interface defining value validation functions. The primary validation function, `of`, takes an
input and returns either a `ValueFailure` or a `ValueObject`. By convention, a value validator implementation is an
abstract class, because the value object's private constructor is often passed to its primary constructor as a
reference.

```kotlin
interface ValueValidator<I, F : ValueFailure<I>, T : ValueObject<I>> {

    fun of(input: I): Either<F, T>

    // ...
}
```

### Examples

The included `StringInRegexValidator` class is an example of a `ValueValidator` implementation.

```kotlin
abstract class StringInRegexValidator<T : ValueObject<String>>(private val ctor: (String) -> T) :
    ValueValidator<String, GenericValueFailure<String>, T> {

    protected abstract val regex: Regex

    override fun of(input: String): Either<GenericValueFailure<String>, T> {
        return if (regex.matches(input)) {
            Either.Right(ctor(input))
        } else {
            Either.Left(GenericValueFailure(input))
        }
    }
}
```

Value object classes can be inlined on the JVM. This `EmailAddress` class is an example of such a `ValueObject`
implementation.

```kotlin
/**
 * A W3C HTML5 email address.
 */
@JvmInline
value class EmailAddress private constructor(override val value: String) : ValueObject<String> {

    companion object : EmailAddressValidator<EmailAddress>(::EmailAddress)
}
```

This value object can then be used to validate an email address like so:

```kotlin
suspend fun createUser(params: UserParamsDto) = either {
    val emailAddress = EmailAddress.of(params.emailAddress).bind()
    // ... validating other params ...
    repositoryCreate(
        UserParams(
            emailAddress = emailAddress
            // ... passing other validated params ...
        )
    ).bind()
}
```

## Annotation Processor

The Target annotation processor library makes it easy to create functionally validated models. It takes the properties
of a model data class and generates:

1. A sealed set of failure classes.
2. A validation function `Companion.of()` using said failure classes.
3. A syntactic sugar function `Companion.only()` when the model contains one or more fields with an `Option` type.

### Failure

The failure class is a sealed interface containing data classes for each value object property declared on the model
template, containing a single value, `parent`, with a type of the value object validator's failure type.

```kotlin
sealed interface ModelFieldFailure {

    data class Property1(val parent: Property1Failure) : ModelFieldFailure

    data class Property2(val parent: Property2Failure) : ModelFieldFailure

    /* ... */
}
```

### Validation Function

The validation function, named `of`, validates the model's fields similar to the behavior of the `ValueValidator` by
taking the raw value object property types and performing cumulative validation, calling each value object's validator
and returning either a non-empty list of model field failures or a model instance.

```kotlin
fun Model.Companion.of(/* ... */): Either<Nel<ModelFieldFailure>, Model>
```

#### Optional Properties

It is also capable of validating optional value objects. This is useful when defining a model builder/update params class
representing updated model fields.

```kotlin
@Validatable
data class ModelBuilder(
    val property1: Option<ModelProperty1>
) {
    companion object
}

fun ModelBuilder.Companion.of(
    property1: Option<RawModelProperty1>
): Either<Nel<ModelBuilderFieldFailure>, ModelBuilder> {
    TODO("...generated validation logic...")
}
```

### Params

The params class is the model class excluding the model template properties annotated with `@External`. It is intended
to contain all the required/non-generated properties used to create a model.

```kotlin
data class ModelParams(/* ... */) {
    companion object {
        fun of(/* ... */): Either<Nel<ModelRequiredFieldFailure>, ModelParams>
    }
}
```

### Nested Models

Nested models are a developing feature. A nested model property is defined just like any other property, with the type
of its model template interface. If the nested model is part of another domain and will be dynamically populated, e.g.,
by a repository, annotate it with `@External`.

```kotlin
@ModelTemplate("Test")
interface TestModel {
    /** ... */

    @External
    val child: TestChildModel?
}

@ModelTemplate("TestChild")
interface TestChildModel {
    /** ... */
}
```

#### Limitations

Nullable nested internal models are not easily updatable using the generated builder class, and are thus not
recommended. In this example, notice the absence of `@External`:

```kotlin
@ModelTemplate("Test")
interface TestModel {
    /** ... */

    val child: TestChildModel?
}

@ModelTemplate("TestChild")
interface TestChildModel {
    /** ... */
}
```

The following builder field is generated:

```kotlin
data class TestBuilder(
    /** ... */

    val child: Option<TestChildBuilder?>
)
```

Now, there are two conflicting use cases:

1. When the intent is to update `TestChild`, but `Test.child` is null.
2. When the intent is to create `TestChild`, but `TestBuilder.child` is not buildable to `TestChildParams`.

There is a consideration to update the generated field to something like:

```kotlin
Option<Either<TestChildBuilder?, TestChildParams>>
```

This would encompass the desired delete, update, and create use cases.

### Usage

Define a model template interface:

```kotlin
@HasPositiveIntId
@HasCreatedAndUpdated
@ModelTemplate("User")
interface UserModel {
    val firstName: FirstName
    val lastName: LastName
    val username: Username?
    val emailAddress: EmailAddress

    @External
    val phoneNumber: UserPhoneNumberModel?
}

@HasCreatedAndUpdated
@ModelTemplate("UserPhoneNumber")
interface UserPhoneNumberModel {
    val userId: PositiveInt
    val number: PhoneNumber
    val validated: Boolean
}
```

Run a build and use the generated classes:

```kotlin
fun createUser() = either {
    repository.create(
        UserParams.of(
            firstName = "John",
            lastName = "Doe",
            username = "john.doe",
            emailAddress = "john.doe@example.com",
        ).bind()
    ).bind()
}

fun greetUser(user: User) {
    println("Hello, ${user.firstName.value}!")
    println("Your account was created on ${user.created}.")
}

fun textUser(user: User, message: SmsTextMessage) = either {
    ensureNotNull(user.phoneNumber) { NoPhoneNumber }.run {
        ensure(validated) { NotValidated }
        sendSms(number, message).bind()
    }
}

fun updateUser(id: PositiveInt) = repository.update(
    id,
    UserBuilder.only(
        username = Some(null)
    )
)
```

## Gradle Setup

> Note that these libraries are experimental, and their APIs are subject to change.

#### Target Core

```kotlin
dependencies {
    implementation("io.target-kt:target-core:$targetVersion")
}
```

#### Target Core + Annotation Processor

```kotlin
plugins {
    id("com.google.devtools.ksp") version kspVersion
}

dependencies {
    implementation("io.target-kt:target-core:$targetVersion")
    compileOnly("io.target-kt:target-annotation:$targetVersion")
    ksp("io.target-kt:target-annotation-processor:$targetVersion")
}
```

See the [KSP docs](https://kotlinlang.org/docs/ksp-overview.html) for additional configuration details.

## Roadmap

1. Add `Parseable` annotation.
    * Add `ValueObjectParser` interface.
    * Generate `parse` function.
