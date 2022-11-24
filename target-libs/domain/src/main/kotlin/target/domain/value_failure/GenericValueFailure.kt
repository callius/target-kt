package target.domain.value_failure

import target.domain.ValueFailure

data class GenericValueFailure<T>(override val failedValue: T) : ValueFailure<T>
