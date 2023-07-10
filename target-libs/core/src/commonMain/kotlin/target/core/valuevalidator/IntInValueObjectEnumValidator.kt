package target.core.valuevalidator

import target.core.ValueObject

abstract class IntInValueObjectEnumValidator<T>(override val all: Collection<T>) :
    GenericInValueObjectsValidator<Int, T>() where T : ValueObject<Int>, T : Enum<T>
