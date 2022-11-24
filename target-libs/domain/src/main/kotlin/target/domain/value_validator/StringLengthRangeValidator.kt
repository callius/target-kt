package target.domain.value_validator

import arrow.core.Either
import target.domain.LengthRange
import target.domain.ValueObject
import target.domain.ValueValidator
import target.domain.value_failure.StringLengthRangeFailure

abstract class StringLengthRangeValidator<T : ValueObject<String>>(private val ctor: (String) -> T) :
    ValueValidator<String, StringLengthRangeFailure, T>,
    LengthRange {

    override fun of(input: String): Either<StringLengthRangeFailure, T> {
        return if (input.length < minLength || input.length > maxLength) {
            Either.Left(StringLengthRangeFailure(input, minLength, maxLength))
        } else {
            Either.Right(ctor(input))
        }
    }
}
