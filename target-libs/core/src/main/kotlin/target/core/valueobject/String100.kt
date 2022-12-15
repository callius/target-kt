package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.StringLengthRangeValidator

@JvmInline
value class String100 private constructor(override val value: String) : ValueObject<String> {

    companion object : StringLengthRangeValidator<String100>(::String100) {
        override val minLength = 1
        override val maxLength = 100
    }
}
