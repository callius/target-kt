package target.annotation

/**
 * Marks an interface as a model template.
 *
 * @param name The base class name of the generated model from which the builder/params will be generated.
 * @param idField The id field to be added to the model.
 * @param customId If [idField] should be ignored in favor of one of the fields on the annotated interface.
 *                 NOTE: Temporary fix accounting for kotlinx-metadata not being able to get
 *                 annotations on a class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelTemplate(
    val name: String,
    // TODO: Remove these properties.
    //  Update readme: update EmailAddress & valueobject removal, add Gradle usage section, update roadmap.
    val idField: AddField = AddField(name = "id", type = PositiveInt::class),
    val customId: Boolean = false
)
