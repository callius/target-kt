package target.domain.value_failure

import target.domain.ValueFailure

data class StringLengthRangeFailure(
    override val failedValue: String,
    val minLength: Int,
    val maxLength: Int,
) : ValueFailure<String>
