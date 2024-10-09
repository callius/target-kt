package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.*

/**
 * Generates a model data class with the given [paramsProperties].
 */
fun generateBuilderSpec(
    builderClassName: ClassName,
    paramsProperties: List<ModelProperty>,
): TypeSpec {
    val optionParams = paramsProperties.map {
        it.copy(
            type = ModelPropertyType.Standard(
                ClassNames.option,
                typeArguments = listOf(ModelPropertyTypeArgument.Type(it.type))
            )
        )
    }

    return TypeSpec.classBuilder(builderClassName)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameters(optionParams.map { ParameterSpec.builder(it.name, it.type.toTypeName()).build() })
                .build()
        )
        .addProperties(
            optionParams.map {
                PropertySpec.builder(it.name, it.type.toTypeName()).initializer(it.name).build()
            }
        )
        .addType(
            TypeSpec.companionObjectBuilder().addFunction(
                FunSpec.builder(FunctionNames.ONLY)
                    .addParameters(
                        optionParams.map {
                            ParameterSpec.builder(it.name, it.type.toTypeName())
                                .defaultValue("%T", ClassNames.none)
                                .build()
                        }
                    )
                    .returns(builderClassName)
                    .addCode(
                        CodeBlock.builder()
                            .rtrn()
                            .constructorCall(builderClassName, paramsProperties, checkVName = false)
                            .build()
                    )
                    .build()
            ).build()
        )
        .build()
}

private fun ModelPropertyType.toTypeName(): TypeName {
    return when (this) {
        is ModelPropertyType.ModelTemplate -> ClassName(
            type.packageName,
            type.simpleName.appendBuilder()
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
