package target.annotation

/**
 * Marks a data class as validatable.
 *
 * Generates an `of` function on the companion object.
 * Generates an `only` function on the companion object when one or more fields have an `Option` type.
 * Generates a `sealed interface {ClassName}FieldFailure` used in generated function return types.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Validatable
