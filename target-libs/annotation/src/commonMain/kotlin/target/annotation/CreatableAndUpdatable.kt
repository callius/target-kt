package target.annotation

/**
 * Combines [Creatable] and [Updatable].
 */
@Updatable
@Creatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreatableAndUpdatable
