package target.annotation

/**
 * Aggregate for [HasCreated] and [HasUpdated].
 */
@HasUpdated
@HasCreated
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class HasCreatedAndUpdated
