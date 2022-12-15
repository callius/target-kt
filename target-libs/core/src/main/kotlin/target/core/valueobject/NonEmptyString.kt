package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.StringNotEmptyValidator

@JvmInline
value class NonEmptyString private constructor(override val value: String) : ValueObject<String> {

    companion object : StringNotEmptyValidator<NonEmptyString>(::NonEmptyString)
}
