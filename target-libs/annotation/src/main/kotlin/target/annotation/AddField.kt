package target.annotation

import kotlin.reflect.KClass

/**
 * Defines a field to add to the generated classes. By default, it only adds the field to the
 * generated model class.
 *
 * @param name The name of the generated field.
 * @param type The type of the generated field.
 * @param ignore If this field should be ignored when generating builder/params.
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AddField(
    val name: String,
    val type: KClass<*>,
    val ignore: Boolean = true
)
