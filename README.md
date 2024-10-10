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

The Target annotation processor library makes it easy to create functionally validated models. It takes the fields
of a model data class and generates:

1. A sealed set of failure classes.
2. A validation function `Model.Companion.of()` using said failure classes.
3. A syntactic sugar function `Model.Companion.only()` when the model contains one or more fields with an `Option` type.

### Failure

The failure class is a sealed interface containing data classes for each value object property declared on the model
template, containing a single value, `parent`, with a type of the value object validator's failure type.

```kotlin
sealed interface ModelFieldFailure {

    data class Property1(val parent: Property1Failure) : ModelFieldFailure

    data class Property2(val parent: Property2Failure) : ModelFieldFailure

   // ...
}
```

### Validation Function

The validation function, named `of`, validates the model's fields similar to the behavior of a `ValueValidator` by
taking the raw value object field types and performing cumulative validation, calling each value object's validator
and returning either a non-empty list of model field failures or a model instance.

```kotlin
fun Model.Companion.of(/* arguments with raw field types */): Either<Nel<ModelFieldFailure>, Model>
```

### Optional Properties

It is also capable of validating optional value objects. This is useful when defining a model builder/update parameters
class representing updated model fields.

In addition to validating optional fields, the annotation processor will generate another function, named `only`, for
partial instantiation, applying a default of `None` to each of those fields. This is useful for only updating some
fields of a model without explicitly setting all others to `None`.

Here's a minimal example:

```kotlin
/**
 * Model builder used to update a model.
 */
@Validatable
data class ModelBuilder(
    val property1: Option<ModelProperty1>
) {
    companion object
}

/**
 * Validation function generated by the processor.
 */
fun ModelBuilder.Companion.of(
    property1: Option<RawModelProperty1>
): Either<Nel<ModelBuilderFieldFailure>, ModelBuilder> {
    TODO("...generated validation logic...")
}

/**
 * Syntactic function generated by the processor.
 */
fun ModelBuilder.Companion.only(
    property1: Option<ModelProperty1> = None
): ModelBuilder = ModelBuilder(property1)

/**
 * Function snippet of a usage example.
 */
fun updateModelProperty1(repository: ModelRepository, id: ModelId, property1: ModelProperty1) {
    repository.updateById(
        id = id,
        builder = ModelBuilder.only(
            property1 = property1.some()
            // ... all other builder properties will be set to None.
        )
    )
}
```

### Nested Models

Nested models are a developing feature. A nested model field is defined just like any other field, with the type
of its model data class. Its definition in the validation function will be as follows:

```kotlin
@Validatable
data class Model(
   val child: ChildModel
) {
   companion object
}

fun Model.Companion.of(
   child: Either<Nel<ChildModelFieldFailure>, ChildModel>
) {
   TODO()
}
```

This delegates the validation of the model to the models own validation function. A failure for it will also be
generated for the parent model:

```kotlin
sealed interface ModelFieldFailure {

   data class Child(val parent: Nel<ChildModelFieldFailure>) : ModelFieldFailure
}
```

### Usage Example

Define a model data class:

```kotlin
@Validatable
data class User(
    val id: PositiveInt,
    val firstName: FirstName,
    val lastName: LastName,
    val username: Username?,
    val emailAddress: EmailAddress,
    val phoneNumber: UserPhoneNumber?,
    val updated: Instant,
    val created: Instant
) {
    companion object
}

@Validatable
data class UserPhoneNumber(
    val userId: PositiveInt,
    val number: PhoneNumber,
    val validated: Boolean,
    val updated: Instant,
    val created: Instant
) {
    companion object
}

@Validatable
data class UserParams(
    val firstName: FirstName,
    val lastName: LastName,
    val username: Username?,
    val emailAddress: EmailAddress,
    val phoneNumber: UserPhoneNumberParams?
) {
    companion object
}

@Validatable
data class UserPhoneNumberParams(
    val number: PhoneNumber,
    val validated: Boolean
) {
    companion object
}

@Validatable
data class UserBuilder(
    val firstName: Option<FirstName>,
    val lastName: Option<LastName>,
    val username: Option<Username?>,
    val emailAddress: Option<EmailAddress>,
    val phoneNumber: Option<UserPhoneNumberBuilder?>
) {
    companion object
}

@Validatable
data class UserPhoneNumberBuilder(
   val number: Option<PhoneNumber>,
   val validated: Option<Boolean>
) {
   companion object
}
```

Run a build and use the generated validation functions:

```kotlin
fun createUser() = either {
   repository.create(
      UserParams.of(
          firstName = "John",
          lastName = "Doe",
          username = "john.doe",
          emailAddress = "john.doe@example.com",
          phoneNumber = UserPhoneNumberParams.of(
              number = "+11231231234",
              validated = false
          )
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
        username = null.some(),
        phoneNumber = UserPhoneNumberBuilder.only(
            validated = true.some()
        ).some()
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
    * Generate `Model.Companion.parse()` function.
2. Convert to compiler plugin and remove the need for `companion object` stubs once a compiler plugin API is released.
