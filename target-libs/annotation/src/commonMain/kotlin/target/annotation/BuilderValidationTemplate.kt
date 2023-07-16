package target.annotation

/**
 * Marks an interface as a builder validation template.
 *
 * @param name The name of the generated function.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BuilderValidationTemplate(
    val name: String
)
