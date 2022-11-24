package target.domain.value_validator

import target.domain.ValueObject

abstract class IntInValueObjectsValidator<T : ValueObject<Int>> : GenericInValueObjectsValidator<Int, T>()
