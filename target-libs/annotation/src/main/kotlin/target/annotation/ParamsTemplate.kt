package target.annotation

/**
 * Generates:
 *   1. A class with all the declared fields and a companion object with the familiar, `of`, function and another,
 *      `ofOptions`, function.
 *   2. A set of sealed interfaces representing field validation failures and missing fields.
 *
 *   ```kotlin
 *   @ParamsTemplate("UserCreateParams")
 *   interface UserCreateParamsModel {
 *      val firstName: FirstName
 *      val lastName: LastName
 *      val emailAddress: EmailAddress
 *      val password: NonBlankString
 *   }
 *
 *   sealed interface UserCreateParamsOfOptionsFailure {
 *      object FirstName : UserCreateParamsOfOptionsFailure
 *      object LastName : UserCreateParamsOfOptionsFailure
 *      object EmailAddress : UserCreateParamsOfOptionsFailure
 *      object NonBlankString : UserCreateParamsOfOptionsFailure
 *   }
 *
 *   sealed interface UserCreateParamsFieldFailure : UserCreateParamsOfOptionsFailure {
 *      /* ... */
 *   }
 *
 *   data class UserCreateParams(
 *      val firstName: FirstName
 *      val lastName: LastName
 *      val emailAddress: EmailAddress
 *      val password: NonBlankString
 *   ) {
 *      companion object {
 *
 *          fun of(
 *              val firstName: String
 *              val lastName: String
 *              val emailAddress: String
 *              val password: String
 *          ): Either<Nel<UserCreateParamsFieldFailure>, UserCreateParams> { /* ... */ }
 *
 *          fun ofOptions(
 *              val firstName: Option<String>
 *              val lastName: Option<String>
 *              val emailAddress: Option<String>
 *              val password: Option<String>
 *          ): Either<Nel<UserCreateParamsOfOptionsFailure>, UserCreateParams> { /* ... */ }
 *      }
 *   }
 *   ```
 */
annotation class ParamsTemplate(val name: String)
