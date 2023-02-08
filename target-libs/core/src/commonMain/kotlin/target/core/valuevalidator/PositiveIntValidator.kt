package target.core.valuevalidator

import target.core.ValueObject

/**
 * Positive integer validator. Useful for ids and is greater than 0.
 * @see PositiveLongValidator
 */
abstract class PositiveIntValidator<T : ValueObject<Int>>(ctor: (Int) -> T) : IntMinValueValidator<T>(ctor) {

    override val minValue = 1
}
