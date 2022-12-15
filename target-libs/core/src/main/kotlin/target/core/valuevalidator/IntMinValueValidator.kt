package target.core.valuevalidator

import target.core.ValueObject

abstract class IntMinValueValidator<T : ValueObject<Int>>(ctor: (Int) -> T) :
    ComparableMinValueValidator<T, Int>(ctor)
