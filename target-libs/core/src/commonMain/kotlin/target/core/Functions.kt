package target.core

import arrow.core.*

/**
 * Validates this list and returns either a list of all failures, or a validated list.
 */
fun <L, R> List<Either<L, R>>.validate(): Either<Nel<L>, List<R>> {
    val failureList = mutableListOf<L>()
    val successList = mutableListOf<R>()
    forEach {
        it.fold(failureList::add, successList::add)
    }

    return failureList.toNonEmptyListOrNull()?.left() ?: successList.right()
}

/**
 * Validates this list and returns either a list of all failures, or a validated list.
 */
fun <L, R> Nel<Either<L, R>>.validate(): Either<Nel<L>, Nel<R>> {
    val failureList = mutableListOf<L>()
    val successList = mutableListOf<R>()
    forEach {
        it.fold(failureList::add, successList::add)
    }

    return failureList.toNonEmptyListOrNull()?.left() ?: successList.toNonEmptyListOrNull()!!.right()
}
