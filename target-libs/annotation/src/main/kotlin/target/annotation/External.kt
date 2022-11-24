package target.annotation

/**
 * Marks a field as external, and will not be present on the params/builder.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class External
