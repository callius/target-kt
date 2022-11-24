package target.annotation

/**
 * Marks a [ModelTemplate] with [HasUpdated] and [HasUpdaterId].
 */
@HasUpdaterId
@HasUpdated
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Updatable
