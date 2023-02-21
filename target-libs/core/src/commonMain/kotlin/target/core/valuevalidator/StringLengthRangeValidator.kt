package target.core.valuevalidator

import arrow.core.Either
import target.core.LengthRange
import target.core.ValueObject
import target.core.ValueValidator
import target.core.valuefailure.StringLengthRangeFailure

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
