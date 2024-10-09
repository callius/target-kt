package target.annotation_processor.core.domain

import com.squareup.kotlinpoet.ClassName

sealed interface TypedModelProperty {

    val property: ModelProperty

    val validatedName: String?

    class Standard private constructor(
        override val property: ModelProperty,
        override val validatedName: String? = null
    ) : TypedModelProperty {

        constructor(property: ModelProperty) : this(
            property = property,
            validatedName = null,
        )
    }

    class ValueObject(
        override val property: ModelProperty,
        val type: ModelPropertyType.ValueObject,
        override val validatedName: String
    ) : TypedModelProperty

    class ValueObjectOption(
        override val property: ModelProperty,
        val type: ModelPropertyType.ValueObjectOption,
        val valueObjectType: ClassName,
        val fieldFailureClassName: ClassName,
        override val validatedName: String
    ) : TypedModelProperty

    class ModelTemplate(
        override val property: ModelProperty,
        val type: ModelPropertyType.ModelTemplate,
        override val validatedName: String
    ) : TypedModelProperty
}
