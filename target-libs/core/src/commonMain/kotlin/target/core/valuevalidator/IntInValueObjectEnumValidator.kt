package target.core.valuevalidator

import target.core.ValueObject

abstract class IntInValueObjectEnumValidator<T>(private val valuesGetter: () -> Array<T>) :
    GenericInValueObjectsValidator<Int, T>() where T : ValueObject<Int>, T : Enum<T> {

    override val all by lazy { valuesGetter().toList() }
}
