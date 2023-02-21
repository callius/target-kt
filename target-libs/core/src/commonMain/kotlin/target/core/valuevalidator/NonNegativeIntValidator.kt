package target.core.valuevalidator

import target.core.ValueObject

/**
 * Non-negative integer validator.
 */
abstract class NonNegativeIntValidator<T : ValueObject<Int>>(ctor: (Int) -> T) : IntMinValueValidator<T>(ctor) {

    override val minValue = 0
}
