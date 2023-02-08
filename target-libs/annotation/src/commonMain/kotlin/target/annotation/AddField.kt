package target.annotation

import kotlin.reflect.KClass

/**
 * Defines a field to add to the generated classes. By default, it only adds the field to the
 * generated model class.
 *
 * This annotation can be used to compose multiple fields, like so:
 *
 * ```kotlin
 * @HasUpdated
 * @AddField("updatedBy", PositiveLong::class, ignore = false)
 * annotation class Updatable
 *
 * @Updatable
 * @ModelTemplate("Record")
 * interface RecordModel {
 *  val information: RecordInformation
 * }
 * ```
 *
 * This example generates a model class with the [AddField] annotations defined by both the [HasUpdated] and the newly
 * defined `Updatable` annotations:
 *
 * ```kotlin
 * val updated: Instant
 * val updatedBy: PositiveLong
 * ```
 *
 * @param name The name of the generated field.
 * @param type The type of the generated field.
 * @param ignore If `true`, this field will only be added to the generated model class.
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AddField(
    val name: String,
    val type: KClass<*>,
    val ignore: Boolean = true
)
