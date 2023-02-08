package target.core.valuevalidator

import target.core.ValueObject

abstract class StringInValueObjectsValidator<T : ValueObject<String>> : GenericInValueObjectsValidator<String, T>()
