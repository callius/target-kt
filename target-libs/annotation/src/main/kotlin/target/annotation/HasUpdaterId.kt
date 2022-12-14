package target.annotation

import target.core.valueobject.PositiveInt

/**
 * Marks a [ModelTemplate] as having an updaterId field. Generated as
 *
 * val updaterId: PositiveInt
 */
@AddField(name = "updaterId", type = PositiveInt::class, ignore = false)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HasUpdaterId
