package target.annotation

/**
 * Marks an interface as a params validation template.
 *
 * @param name The name of the generated function.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ParamsValidationTemplate(
    val name: String
)
