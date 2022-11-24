package target.domain.value_validator

import target.domain.ValueObject

abstract class IntInValueObjectEnumValidator<T>(private val valuesGetter: () -> Array<T>) :
    GenericInValueObjectsValidator<Int, T>() where T : ValueObject<Int>, T : Enum<T> {

    override val all by lazy { valuesGetter().toList() }
}
