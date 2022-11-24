package target.domain

import arrow.core.Either

interface Modelable<F, T> {

    /**
     * Creates a model from this.
     */
    fun toModel(): Either<F, T>
}
