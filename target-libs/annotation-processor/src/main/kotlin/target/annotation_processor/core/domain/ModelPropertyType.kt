package target.annotation_processor.core.domain

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

sealed interface ModelPropertyType {

    /**
     * Type of the model property. When the type is a model template, this is the name of the model.
     */
    val type: ClassName

    data class ValueObject(

        override val type: ClassName,

        /**
         * The type of the value contained in the value object.
         */
        val valueObjectType: TypeName
    ) : ModelPropertyType

    data class Standard(

        override val type: ClassName,

        /**
         * Type arguments for this type.
         */
        val typeArguments: List<ModelPropertyTypeArgument>
    ) : ModelPropertyType

    data class ModelTemplate(override val type: ClassName) : ModelPropertyType
}
