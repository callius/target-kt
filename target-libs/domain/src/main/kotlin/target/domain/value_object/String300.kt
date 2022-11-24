package target.domain.value_object

import target.domain.ValueObject
import target.domain.value_validator.StringLengthRangeValidator

@JvmInline
value class String300 private constructor(override val value: String) : ValueObject<String> {

    companion object : StringLengthRangeValidator<String300>(::String300) {
        override val minLength = 1
        override val maxLength = 300
    }
}
