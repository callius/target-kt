package target.core.valuevalidator

import target.core.ValueObject

abstract class StringInValueObjectEnumValidator<T>(override val all: Collection<T>) :
    GenericInValueObjectsValidator<String, T>() where T : ValueObject<String>, T : Enum<T>
