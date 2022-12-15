package target.core.valuevalidator

import arrow.core.Either
import target.core.ValueObject
import target.core.ValueValidator
import target.core.valuefailure.GenericValueFailure

abstract class StringLengthValidator<T : ValueObject<String>>(private val ctor: (String) -> T) :
    ValueValidator<String, GenericValueFailure<String>, T> {

    abstract val length: Int

    override fun of(input: String): Either<GenericValueFailure<String>, T> {
        return if (input.length == length) {
            Either.Right(ctor(input))
        } else {
            Either.Left(GenericValueFailure(input))
        }
    }
}
