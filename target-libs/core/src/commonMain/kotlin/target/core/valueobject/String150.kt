package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.StringLengthRangeValidator

@JvmInline
value class String150 private constructor(override val value: String) : ValueObject<String> {

    companion object : StringLengthRangeValidator<String150>(::String150) {
        override val minLength = 1
        override val maxLength = 150
    }
}
