package target.domain.value_object

import target.domain.ValueObject
import target.domain.value_validator.StringNotEmptyValidator

@JvmInline
value class NonEmptyString private constructor(override val value: String) : ValueObject<String> {

    companion object : StringNotEmptyValidator<NonEmptyString>(::NonEmptyString)
}
