package target.domain.value_validator

import arrow.core.Either
import arrow.core.firstOrNone
import target.domain.ValueObject
import target.domain.ValueValidator
import target.domain.value_failure.GenericValueFailure

abstract class GenericInValueObjectsValidator<I, T : ValueObject<I>> :
    ValueValidator<I, GenericValueFailure<I>, T> {

    abstract val all: Collection<T>

    override fun of(input: I): Either<GenericValueFailure<I>, T> {
        return all.firstOrNone { it.value == input }.toEither { GenericValueFailure(input) }
    }
}
