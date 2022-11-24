package target.domain.value_validator

import arrow.core.Either
import target.domain.ValueObject
import target.domain.ValueValidator
import target.domain.value_failure.GenericValueFailure

abstract class ShortRangeValidator<T : ValueObject<Short>>(private val ctor: (Short) -> T) :
    ValueValidator<Short, GenericValueFailure<Short>, T> {

    abstract val min: Short
    abstract val max: Short

    override fun of(input: Short): Either<GenericValueFailure<Short>, T> {
        return if (input < min || input > max) {
            Either.Left(GenericValueFailure(input))
        } else {
            Either.Right(ctor(input))
        }
    }
}
