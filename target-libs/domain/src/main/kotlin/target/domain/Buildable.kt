package target.domain

import arrow.core.Option

interface Buildable<T> {

    /**
     * Builds [T] from this. Analogous to a zip function on all the builder's properties passed
     * to the constructor of [T].
     */
    fun build(): Option<T>
}
