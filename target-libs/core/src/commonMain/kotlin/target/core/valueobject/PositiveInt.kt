package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.IntMinValueValidator

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
