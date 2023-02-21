package target.core.valuevalidator

import target.core.ValueObject

abstract class LongMinValueValidator<T : ValueObject<Long>>(ctor: (Long) -> T) :
    ComparableMinValueValidator<T, Long>(ctor)
