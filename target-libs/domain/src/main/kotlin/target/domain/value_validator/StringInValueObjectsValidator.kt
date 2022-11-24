package target.domain.value_validator

import target.domain.ValueObject

abstract class StringInValueObjectsValidator<T : ValueObject<String>> : GenericInValueObjectsValidator<String, T>()
