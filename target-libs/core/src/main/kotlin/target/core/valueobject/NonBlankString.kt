package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.StringNotBlankValidator

@JvmInline
value class NonBlankString private constructor(override val value: String) : ValueObject<String> {

    companion object : StringNotBlankValidator<NonBlankString>(::NonBlankString)
}
