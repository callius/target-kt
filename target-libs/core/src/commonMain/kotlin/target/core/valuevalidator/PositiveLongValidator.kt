package target.core.valuevalidator

import target.core.ValueObject

/**
 * Positive long validator. Useful for ids and is greater than 0.
 * @see PositiveIntValidator
 */
abstract class PositiveLongValidator<T : ValueObject<Long>>(ctor: (Long) -> T) : LongMinValueValidator<T>(ctor) {

    override val minValue = 1L
}
