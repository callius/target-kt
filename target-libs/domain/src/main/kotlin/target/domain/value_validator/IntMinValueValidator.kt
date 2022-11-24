package target.domain.value_validator

import target.domain.ValueObject

abstract class IntMinValueValidator<T : ValueObject<Int>>(ctor: (Int) -> T) :
    ComparableMinValueValidator<T, Int>(ctor)
