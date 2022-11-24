package target.domain.value_validator

import arrow.core.Either
import target.domain.ValueObject
import target.domain.ValueValidator
import target.domain.value_failure.GenericValueFailure

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
