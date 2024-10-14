package target.annotation_processor.core.domain

/**
 * A property defined by an interface annotated with @ModelTemplate.
 */
data class ModelProperty(

    /**
     * The name of the property.
     */
    val name: String,

    /**
     * The type of the property.
     */
    val type: ModelPropertyType,
) {

    /**
     * The validated name of this property.
     */
    val vName: String get() = 'v' + name.replaceFirstChar { it.uppercaseChar() }

    fun vNameIfNullable() = if (type.type.isNullable) vName else name
}
