package target.domain.value_validator

import target.domain.ValueObject

abstract class StringInValueObjectEnumValidator<T>(private val valuesGetter: () -> Array<T>) :
    GenericInValueObjectsValidator<String, T>() where T : ValueObject<String>, T : Enum<T> {

    override val all by lazy { valuesGetter().toList() }
}
