package target.annotation

/**
 * Marks an interface as a validation template.
 *
 * @param name The name of the generated function.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ValidationTemplate(
    val name: String
)
