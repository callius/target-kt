package target.annotation

import target.core.valueobject.PositiveInt

/**
 * Marks a [ModelTemplate] as having a creatorId field. Generated as:
 * val creatorId: PositiveInt
 */
@AddField(name = "creatorId", type = PositiveInt::class, ignore = false)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HasCreatorId
