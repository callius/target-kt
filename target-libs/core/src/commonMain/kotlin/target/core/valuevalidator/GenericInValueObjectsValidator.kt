package target.core.valuevalidator

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import target.core.ValueObject
import target.core.ValueValidator
import target.core.valuefailure.GenericValueFailure

abstract class GenericInValueObjectsValidator<I, T : ValueObject<I>> :
    ValueValidator<I, GenericValueFailure<I>, T> {

    abstract val all: Collection<T>

    override fun of(input: I): Either<GenericValueFailure<I>, T> {
        return all.firstOrNull { it.value == input }?.right() ?: GenericValueFailure(input).left()
    }
}
