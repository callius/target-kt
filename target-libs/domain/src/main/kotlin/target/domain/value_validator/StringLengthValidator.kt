package target.domain.value_validator

import arrow.core.Either
import target.domain.ValueObject
import target.domain.ValueValidator
import target.domain.value_failure.GenericValueFailure

abstract class StringLengthValidator<T : ValueObject<String>>(private val ctor: (String) -> T) :
    ValueValidator<String, GenericValueFailure<String>, T> {

    abstract val length: Int

    override fun of(input: String): Either<GenericValueFailure<String>, T> {
        return if (input.length == length) {
            Either.Right(ctor(input))
        } else {
            Either.Left(GenericValueFailure(input))
        }
    }
}
