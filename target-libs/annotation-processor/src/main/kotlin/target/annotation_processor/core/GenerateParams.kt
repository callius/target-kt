package target.annotation_processor.core

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.appendParams
import target.annotation_processor.core.extension.withNullability
import target.annotation_processor.core.extension.withTypeArguments

/**
 * Generates a params data class with the given [properties].
 */
fun generateParamsSpec(
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
        getModelPropertyFailure = { requiredFieldFailureClassName }
    )
}

private fun ModelPropertyType.toTypeName(): TypeName {
    return when (this) {
        is ModelPropertyType.ModelTemplate -> ClassName(
            type.packageName,
            type.simpleName.appendParams()
        ).withNullability(type.isNullable)

        is ModelPropertyType.Standard -> type.withTypeArguments(
            typeArguments.map {
                when (it) {
                    ModelPropertyTypeArgument.Star -> STAR
                    is ModelPropertyTypeArgument.Type -> it.parent.toTypeName()
                }
            }
        )

        is ModelPropertyType.ValueObject -> type
    }
}

private fun ModelPropertyType.toValueObjectTypeName(): TypeName {
    return when (this) {
        is ModelPropertyType.ModelTemplate -> eitherOf(
            nelOf(requiredFieldFailureType),
            ClassName(type.packageName, type.simpleName.appendParams())
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
