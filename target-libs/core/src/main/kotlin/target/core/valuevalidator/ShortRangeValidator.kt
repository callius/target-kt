package target.core.valuevalidator

import arrow.core.Either
import target.core.ValueObject
import target.core.ValueValidator
import target.core.valuefailure.GenericValueFailure

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
