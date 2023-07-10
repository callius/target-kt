package target.annotation_processor.core.domain

sealed interface ModelPropertyTypeArgument {

    data object Star : ModelPropertyTypeArgument

    data class Type(val parent: ModelPropertyType) : ModelPropertyTypeArgument
}
