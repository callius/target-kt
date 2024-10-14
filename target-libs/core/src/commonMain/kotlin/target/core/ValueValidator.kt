package target.core

import arrow.core.Either
import arrow.core.Either.Right
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

interface ValueValidator<I, F : ValueFailure<I>, T : ValueObject<I>> {

    fun of(input: I): Either<F, T>

    fun ofNullable(input: I?): Either<F, T?> = input?.let(::of) ?: Right(null)

    fun of(input: Option<I>): Either<F, Option<T>> = input.fold({ Right(None) }) { of(it).map(::Some) }

    fun ofNullable(input: Option<I?>): Either<F, Option<T?>> =
        input.fold({ Right(None) }) { ofNullable(it).map(::Some) }
}
