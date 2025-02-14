package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.validateModel
import target.annotation_processor.core.extension.withNullability
import target.annotation_processor.core.extension.withTypeArguments

fun generateCompanionOfSpec(
    failureClassName: ClassName,
    modelClassName: ClassName,
    companionObjectClassName: ClassName,
    properties: List<ModelProperty>
): FunSpec {
    return FunSpec.builder(FunctionNames.OF)
        .receiver(companionObjectClassName)
        .addParameters(
            properties.map {
                ParameterSpec.builder(it.name, it.type.toValueObjectTypeName()).build()
            }
        )
        .returns(eitherOf(nelOf(failureClassName), modelClassName))
        .addCode(
            CodeBlock.builder().validateModel(
                properties = properties,
                model = modelClassName
            ).build()
        )
        .build()
}

private fun ModelPropertyType.toValueObjectTypeName(): TypeName {
    return when (this) {
        is ModelPropertyType.ModelTemplate -> eitherOf(
            nelOf(fieldFailureType),
            type.withNullability(false)
        ).withNullability(type.isNullable)

        is ModelPropertyType.ModelTemplateOption -> optionOf(
            eitherOf(
                nelOf(fieldFailureType),
                modelType.withNullability(false)
            ).withNullability(modelType.isNullable)
        )

        is ModelPropertyType.Standard -> type.withTypeArguments(
            typeArguments.map {
                when (it) {
                    ModelPropertyTypeArgument.Star -> STAR
                    is ModelPropertyTypeArgument.Type -> it.parent.toValueObjectTypeName()
                }
            }
        )

        is ModelPropertyType.ValueObject -> valueObjectValueType
        is ModelPropertyType.ValueObjectOption -> optionOf(valueObjectValueType)

        is ModelPropertyType.ModelTemplateList -> listNameOf(
            eitherOf(
                nelOf(fieldFailureType),
                modelType.withNullability(false)
            )
        ).withNullability(type.isNullable)

        is ModelPropertyType.ModelTemplateNel -> nelOf(
            eitherOf(
                nelOf(fieldFailureType),
                modelType.withNullability(false)
            )
        ).withNullability(type.isNullable)
    }
}
