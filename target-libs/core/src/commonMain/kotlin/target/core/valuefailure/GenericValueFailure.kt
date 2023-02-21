package target.core.valuefailure

import target.core.ValueFailure

data class GenericValueFailure<T>(override val failedValue: T) : ValueFailure<T>
