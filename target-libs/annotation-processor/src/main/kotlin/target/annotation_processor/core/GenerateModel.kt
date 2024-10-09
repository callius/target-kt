package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.validateModel
import target.annotation_processor.core.extension.withNullability
import target.annotation_processor.core.extension.withTypeArguments

/**
 * Generates a model data class with the given [properties].
 */
fun generateModelSpec(
    failureClassName: ClassName,
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
        .addType(
            TypeSpec.companionObjectBuilder().addFunction(
                FunSpec.builder(FunctionNames.OF)
                    .addParameters(
                        properties.map {
                            ParameterSpec.builder(it.name, it.type.toValueObjectTypeName()).build()
                        }
                    )
                    .returns(eitherOf(nelOf(failureClassName), modelClassName))
                    .addCode(
                        CodeBlock.builder().validateModel(
                            properties = properties,
                            model = modelClassName,
                            getModelPropertyFailure = { fieldFailureClassName }
                        ).build()
                    )
                    .build()
            ).build()
        )
        .build()
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
