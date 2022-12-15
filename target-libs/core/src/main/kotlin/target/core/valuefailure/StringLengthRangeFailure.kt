package target.core.valuefailure

import target.core.ValueFailure

data class StringLengthRangeFailure(
    override val failedValue: String,
    val minLength: Int,
    val maxLength: Int,
) : ValueFailure<String>
