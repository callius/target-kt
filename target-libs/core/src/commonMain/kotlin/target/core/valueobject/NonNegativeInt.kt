package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.IntMinValueValidator

/**
 * Non-negative integer.
 */
@JvmInline
value class NonNegativeInt private constructor(override val value: Int) : ValueObject<Int> {

    companion object : IntMinValueValidator<NonNegativeInt>(::NonNegativeInt) {
        val zero by lazy { NonNegativeInt(0) }
        val one by lazy { NonNegativeInt(1) }

        override val minValue = 0
    }

    operator fun plus(other: NonNegativeInt) = NonNegativeInt(value + other.value)
}
