package target.annotation

import target.core.ValueFailure
import target.core.valueobject.PositiveInt
import kotlin.reflect.KClass

/**
 * Marks an interface as a model template.
 *
 * @param name The base class name of the generated model from which the builder/params will be generated.
 * @param failure The failure class to return when validation returns a [ValueFailure].
 *   Defaults to [ValueFailure] which does not map the left result.
 * @param idField The id field to be added to the model.
 * @param customId If [idField] should be ignored in favor of one of the fields on the annotated interface.
 *                 NOTE: Temporary fix accounting for kotlinx-metadata not being able to get
 *                 annotations on a class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelTemplate(
    val name: String,
    val failure: KClass<*> = ValueFailure::class,
    val idField: AddField = AddField(name = "id", type = PositiveInt::class),
    val customId: Boolean = false
)
