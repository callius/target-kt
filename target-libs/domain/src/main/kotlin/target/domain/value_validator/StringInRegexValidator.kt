package target.domain.value_validator

import arrow.core.Either
import target.domain.ValueObject
import target.domain.ValueValidator
import target.domain.value_failure.GenericValueFailure

abstract class StringInRegexValidator<T : ValueObject<String>>(private val ctor: (String) -> T) :
    ValueValidator<String, GenericValueFailure<String>, T> {

    protected abstract val regex: Regex

    override fun of(input: String): Either<GenericValueFailure<String>, T> {
        return if (regex.matches(input)) {
            Either.Right(ctor(input))
        } else {
            Either.Left(GenericValueFailure(input))
        }
    }
}
