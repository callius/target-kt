package target.domain.value_object

import target.domain.ValueObject
import target.domain.value_validator.IntMinValueValidator

/**
 * Positive integer. Used for ids and is greater than 0.
 */
@JvmInline
value class PositiveInt private constructor(override val value: Int) : ValueObject<Int> {

    companion object : IntMinValueValidator<PositiveInt>(::PositiveInt) {
        val one by lazy { PositiveInt(1) }

        override val minValue = 1
    }

    operator fun plus(other: PositiveInt) = PositiveInt(value + other.value)
}
