package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.StringLengthRangeValidator

@JvmInline
value class String500 private constructor(override val value: String) : ValueObject<String> {

    companion object : StringLengthRangeValidator<String500>(::String500) {
        override val minLength = 1
        override val maxLength = 500
    }
}
