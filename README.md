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

A `ValueObject` is an interface representing a validated value. Value objects, by convention, have a private primary
constructor, so that they are not instantiated outside a `ValueValidator`. A value object implementation must declare a
companion object implementing a value validator when used in conjunction with the annotation processor library.

```kotlin
interface ValueObject<T> {
    val value: T
}
```

#### Value Validator

A `ValueValidator` is an interface defining value validation functions. The primary validation function,`of`, takes an
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

The included `EmailAddress` class is an example of a `ValueObject` implementation.

```kotlin
/**
 * A W3C HTML5 email address.
 */
@JvmInline
value class EmailAddress private constructor(override val value: String) : ValueObject<String> {

    companion object : StringInRegexValidator<EmailAddress>(::EmailAddress) {

        override val regex by lazy {
            Regex(
                """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
            )
        }
    }
}
```

## Annotation Processor

The target annotation processor library takes the properties of a model template interface and generates three classes:
model, params, and builder.

#### Model

The model class is the complete model and contains a validation function, `of`, similar to `ValueValidator` which takes
the raw value object property types and performs a zip operation, calling each value object's validator and returning
either the failure defined by the `ModelTemplate` annotation (`ValueFailure<*>` by default) or a model instance.

```kotlin
data class Model(/* ... */) {
    companion object {
        fun of(/* ... */): Either<ValueFailure<*>, Model>
    }
}
```

#### Params

The params class is the model class excluding the model template properties annotated with `@External`. It is intended
to contain all the required/non-generated properties used to create a model.

```kotlin
data class ModelParams(/* ... */) {
    companion object {
        fun of(/* ... */): Either<ValueFailure<*>, ModelParams>
    }
}
```

#### Builder

The builder class is the params class with each property wrapped in an `Option` and implements the `Buildable` interface
by performing a zip operation on its properties. It is intended to be used to perform a partial update operation on a
model. It also contains a convenience `only` function, which delegates to the primary constructor with each parameter
defaulting to `None`.

```kotlin
data class ModelBuilder(/* ... */) : Buildable<ModelParams> {
    override fun build(): Option<ModelParams>

    companion object {
        fun of(/* ... */): Either<ValueFailure<*>, ModelBuilder>

        fun only(/* ... */): ModelBuilder
    }
}
```

### Example

```kotlin
@HasCreatedAndUpdated
@ModelTemplate("User")
interface UserModel {
    val firstName: FirstName
    val lastName: LastName
    val username: Username?
    val emailAddress: EmailAddress
}
```

Generates:

```kotlin
data class User(
    val id: PositiveInt,
    val firstName: FirstName,
    val lastName: LastName,
    val username: Username?,
    val emailAddress: EmailAddress,
    val updated: Instant,
    val created: Instant
) {
    companion object {
        fun of(
            id: Int,
            firstName: String,
            lastName: String,
            username: String?,
            emailAddress: String,
            updated: Instant,
            created: Instant
        ): Either<ValueFailure<*>, User> = PositiveInt.of(id).flatMap { vId ->
            FirstName.of(firstName).flatMap { vFirstName ->
                LastName.of(lastName).flatMap { vLastName ->
                    Username.ofNullable(username).flatMap { vUsername ->
                        EmailAddress.of(emailAddress).map { vEmailAddress ->
                            User(vId, vFirstName, vLastName, vUsername, vEmailAddress, updated, created)
                        }
                    }
                }
            }
        }
    }
}

data class UserParams(
    val firstName: FirstName,
    val lastName: LastName,
    val username: Username?,
    val emailAddress: EmailAddress
) {
    companion object {
        fun of(
            firstName: String,
            lastName: String,
            username: String?,
            emailAddress: String
        ): Either<ValueFailure<*>, UserParams> = FirstName.of(firstName).flatMap { vFirstName ->
            LastName.of(lastName).flatMap { vLastName ->
                Username.ofNullable(username).flatMap { vUsername ->
                    EmailAddress.of(emailAddress).map { vEmailAddress ->
                        UserParams(vId, vFirstName, vLastName, vUsername, vEmailAddress)
                    }
                }
            }
        }
    }
}

data class UserBuilder(
    val firstName: Option<FirstName>,
    val lastName: Option<LastName>,
    val username: Option<Username?>,
    val emailAddress: Option<EmailAddress>
) : Buildable<UserParams> {
    override fun build(): Option<UserParams> = firstName.flatMap { vFirstName ->
        lastName.flatMap { vLastName ->
            username.flatMap { vUsername ->
                emailAddress.map { vEmailAddress ->
                    UserParams(vId, vFirstName, vLastName, vUsername, vEmailAddress)
                }
            }
        }
    }

    companion object {
        fun of(
            firstName: Option<String>,
            lastName: Option<String>,
            username: Option<String?>,
            emailAddress: Option<String>
        ): Either<ValueFailure<*>, UserBuilder> = FirstName.of(firstName).flatMap { vFirstName ->
            LastName.of(lastName).flatMap { vLastName ->
                Username.ofNullable(username).flatMap { vUsername ->
                    EmailAddress.of(emailAddress).map { vEmailAddress ->
                        UserBuilder(vId, vFirstName, vLastName, vUsername, vEmailAddress)
                    }
                }
            }
        }

        fun only(
            firstName: Option<FirstName> = None,
            lastName: Option<LastName> = None,
            username: Option<Username?> = None,
            emailAddress: Option<EmailAddress> = None
        ): UserBuilder = UserBuilder(firstName, lastName, username, emailAddress)
    }
}
```

## Roadmap

1. Generate cumulative model validation functions, instead of the current fail-fast behavior. (in QA)
2. Clean up `ModelTemplate` and other annotations for a more intuitive experience.
3. Add a configuration property to define the generated timestamp implementation type (currently `java.time.Instant`)
   and support `kotlinx.datetime.Instant` as the new default.
4. Support Kotlin Multiplatform.
