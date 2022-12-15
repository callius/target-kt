package target.core.valuevalidator

import target.core.ValueObject

abstract class StringInValueObjectEnumValidator<T>(private val valuesGetter: () -> Array<T>) :
    GenericInValueObjectsValidator<String, T>() where T : ValueObject<String>, T : Enum<T> {

    override val all by lazy { valuesGetter().toList() }
}
