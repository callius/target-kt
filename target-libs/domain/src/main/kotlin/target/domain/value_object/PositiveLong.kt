package target.domain.value_object

import target.domain.ValueObject
import target.domain.value_validator.LongMinValueValidator

/**
 * Positive integer. Used for ids and is greater than 0.
 */
@JvmInline
value class PositiveLong private constructor(override val value: Long) : ValueObject<Long> {

    companion object : LongMinValueValidator<PositiveLong>(::PositiveLong) {
        val one by lazy { PositiveLong(1) }

        override val minValue = 1L
    }

    operator fun plus(other: PositiveLong) = PositiveLong(value + other.value)
}
