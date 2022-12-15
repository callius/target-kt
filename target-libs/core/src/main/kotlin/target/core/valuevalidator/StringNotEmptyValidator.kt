package target.core.valuevalidator

import arrow.core.Either
import target.core.ValueObject
import target.core.ValueValidator
import target.core.valuefailure.GenericValueFailure

abstract class StringNotEmptyValidator<T : ValueObject<String>>(private val ctor: (String) -> T) :
    ValueValidator<String, GenericValueFailure<String>, T> {

    override fun of(input: String): Either<GenericValueFailure<String>, T> {
        return if (input.isEmpty()) {
            Either.Left(GenericValueFailure(input))
        } else {
            Either.Right(ctor(input))
        }
    }
}
