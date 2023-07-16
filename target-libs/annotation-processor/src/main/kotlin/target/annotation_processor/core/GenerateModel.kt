package target.annotation_processor.core

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.withNullability
import target.annotation_processor.core.extension.withTypeArguments

/**
 * Generates a model data class with the given [properties].
 */
fun generateModelSpec(
    failureClassName: ClassName,
    modelClassName: ClassName,
    properties: List<ModelProperty>,
    validationFunctions: List<ValidationFunction>
): TypeSpec {
    return generateModelSpecBase(
        failureClassName = failureClassName,
        modelClassName = modelClassName,
        properties = properties,
        validationFunctions = validationFunctions,
        toTypeName = ModelPropertyType::toTypeName,
        toValueObjectTypeName = ModelPropertyType::toValueObjectTypeName,
        getModelPropertyFailure = { fieldFailureClassName }
    )
}

private fun ModelPropertyType.toTypeName(): TypeName {
    return when (this) {
        is ModelPropertyType.ModelTemplate,
        is ModelPropertyType.ValueObject -> type

        is ModelPropertyType.Standard -> type.withTypeArguments(
            typeArguments.map {
                when (it) {
                    ModelPropertyTypeArgument.Star -> STAR
                    is ModelPropertyTypeArgument.Type -> it.parent.toTypeName()
                }
            }
        )
    }
}

private fun ModelPropertyType.toValueObjectTypeName(): TypeName {
    return when (this) {
        is ModelPropertyType.ModelTemplate -> eitherOf(
            nelOf(fieldFailureType),
            type.withNullability(false)
        ).withNullability(type.isNullable)

        is ModelPropertyType.Standard -> type.withTypeArguments(
            typeArguments.map {
                when (it) {
                    ModelPropertyTypeArgument.Star -> STAR
                    is ModelPropertyTypeArgument.Type -> it.parent.toValueObjectTypeName()
                }
            }
        )

        is ModelPropertyType.ValueObject -> valueObjectType
    }
}
