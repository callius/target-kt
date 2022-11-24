package target.domain.value_validator

import target.domain.ValueObject

abstract class LongMinValueValidator<T : ValueObject<Long>>(ctor: (Long) -> T) :
    ComparableMinValueValidator<T, Long>(ctor)
