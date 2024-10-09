package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.ModelProperty
import target.annotation_processor.core.domain.ModelPropertyType
import target.annotation_processor.core.domain.ModelPropertyTypeArgument
import target.annotation_processor.core.extension.appendParams
import target.annotation_processor.core.extension.withNullability
import target.annotation_processor.core.extension.withTypeArguments

/**
 * Generates a params data class with the given [properties].
 */
fun generateParamsSpec(
    modelClassName: ClassName,
    properties: List<ModelProperty>
): TypeSpec {
    return TypeSpec.classBuilder(modelClassName)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec.constructorBuilder().addParameters(
                properties.map {
                    ParameterSpec.builder(it.name, it.type.toTypeName()).build()
                }
            ).build()
        )
        .addProperties(
            properties.map {
                PropertySpec.builder(it.name, it.type.toTypeName()).initializer(it.name).build()
            }
        )
        .build()
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
