package target.core.valuevalidator

import arrow.core.Either
import target.core.ValueObject
import target.core.ValueValidator
import target.core.valuefailure.GenericValueFailure

abstract class ComparableMinValueValidator<T : ValueObject<C>, C : Comparable<C>>(private val ctor: (C) -> T) :
    ValueValidator<C, GenericValueFailure<C>, T> {

    abstract val minValue: C

    override fun of(input: C): Either<GenericValueFailure<C>, T> {
        return if (input >= minValue) {
            Either.Right(ctor(input))
        } else {
            Either.Left(GenericValueFailure(input))
        }
    }
}
