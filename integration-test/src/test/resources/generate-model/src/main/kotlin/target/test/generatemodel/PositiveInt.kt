package target.test.generatemodel

import target.core.ValueObject
import target.core.valuevalidator.PositiveIntValidator

@JvmInline
value class PositiveInt private constructor(override val value: Int) : ValueObject<Int> {

    companion object : PositiveIntValidator<PositiveInt>(::PositiveInt)
}
