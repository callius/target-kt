package target.domain.value_validator

import arrow.core.Either
import target.domain.ValueObject
import target.domain.ValueValidator
import target.domain.value_failure.GenericValueFailure

abstract class StringNotBlankValidator<T : ValueObject<String>>(private val ctor: (String) -> T) :
    ValueValidator<String, GenericValueFailure<String>, T> {

    override fun of(input: String): Either<GenericValueFailure<String>, T> {
        return if (input.isBlank()) {
            Either.Left(GenericValueFailure(input))
        } else {
            Either.Right(ctor(input))
        }
    }
}
