package target.annotation

/**
 * Marks a [ModelTemplate] with [HasCreated] and [HasCreatorId].
 */
@HasCreatorId
@HasCreated
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Creatable
