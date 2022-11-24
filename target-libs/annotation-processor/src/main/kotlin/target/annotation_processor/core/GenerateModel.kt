package target.annotation_processor.core

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import target.annotation_processor.core.domain.ModelProperty
import target.annotation_processor.core.domain.ModelPropertyType
import target.annotation_processor.core.domain.ModelPropertyTypeArgument
import target.annotation_processor.core.extension.withTypeArguments

/**
 * Generates a model data class with the given [properties].
 */
fun generateModelSpec(
    failureClassName: ClassName,
    modelClassName: ClassName,
    properties: List<ModelProperty>
): TypeSpec {
    return generateModelSpecBase(
        failureClassName = failureClassName,
        modelClassName = modelClassName,
        properties = properties,
        toTypeName = ModelPropertyType::toTypeName,
        toValueObjectTypeName = ModelPropertyType::toValueObjectTypeName
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
        is ModelPropertyType.ModelTemplate -> type
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
