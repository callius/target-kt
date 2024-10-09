package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.constructorCall
import target.annotation_processor.core.extension.rtrn
import target.annotation_processor.core.extension.withTypeArguments

fun generateCompanionOnlySpec(
    modelClassName: ClassName,
    companionObjectClassName: ClassName,
    properties: List<ModelProperty>
): FunSpec {
    return FunSpec.builder(FunctionNames.ONLY)
        .receiver(companionObjectClassName)
        .addParameters(
            properties.map {
                when (it.type) {
                    is ModelPropertyType.ModelTemplate,
                    is ModelPropertyType.ValueObject -> ParameterSpec.builder(it.name, it.type.toTypeName())
                        .build()

                    is ModelPropertyType.ValueObjectOption -> optionParameter(it.name, it.type.toTypeName())

                    is ModelPropertyType.Standard -> when (it.type.type) {
                        ClassNames.option -> optionParameter(it.name, it.type.toTypeName())

                        else -> ParameterSpec.builder(it.name, it.type.toTypeName()).build()
                    }
                }
            }
        )
        .returns(modelClassName)
        .addCode(
            CodeBlock.builder()
                .rtrn()
                .constructorCall(modelClassName, properties, checkVName = false)
                .build()
        )
        .build()
}

private fun optionParameter(name: String, type: TypeName) = ParameterSpec.builder(name, type)
    .defaultValue("%T", ClassNames.none)
    .build()

private fun ModelPropertyType.toTypeName(): TypeName {
    return when (this) {
        is ModelPropertyType.Standard -> type.withTypeArguments(
            typeArguments.map {
                when (it) {
                    ModelPropertyTypeArgument.Star -> STAR
                    is ModelPropertyTypeArgument.Type -> it.parent.toTypeName()
                }
            }
        )

        is ModelPropertyType.ModelTemplate,
        is ModelPropertyType.ValueObject -> type

        is ModelPropertyType.ValueObjectOption -> optionOf(valueObjectType)
    }
}
