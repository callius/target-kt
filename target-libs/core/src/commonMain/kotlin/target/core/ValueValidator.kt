package target.core

import arrow.core.Either
import arrow.core.Option

interface ValueValidator<I, F : ValueFailure<I>, T : ValueObject<I>> {

    fun of(input: I): Either<F, T>

    fun ofNullable(input: I?): Either<F, T?> = input?.let(::of) ?: Either.Right(null)

    fun of(input: Option<I>): Either<F, Option<T>> = input.traverse(::of)

    fun ofNullable(input: Option<I?>): Either<F, Option<T?>> = input.traverse(::ofNullable)
}
