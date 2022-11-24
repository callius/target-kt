package target.annotation_processor.core.domain

sealed interface ModelPropertyTypeArgument {

    object Star : ModelPropertyTypeArgument

    data class Type(val parent: ModelPropertyType) : ModelPropertyTypeArgument
}
