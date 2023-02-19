package target.annotation

/**
 * Marks an interface as a model template.
 *
 * @param name The base class name of the generated model from which the builder/params will be generated.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelTemplate(
    val name: String
)
