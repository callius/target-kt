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

    /**
     * If the property is external.
     * In most cases, an external property will be annotation with the @External annotation.
     */
    val isExternal: Boolean,
) {

    /**
     * The validated name of this property.
     */
    val vName: String get() = 'v' + name.replaceFirstChar { it.uppercaseChar() }

    val isNotExternal: Boolean get() = !isExternal
}
