package target.domain.value_object

import target.domain.ValueObject
import target.domain.value_validator.StringNotBlankValidator

@JvmInline
value class NonBlankString private constructor(override val value: String) : ValueObject<String> {

    companion object : StringNotBlankValidator<NonBlankString>(::NonBlankString)
}
