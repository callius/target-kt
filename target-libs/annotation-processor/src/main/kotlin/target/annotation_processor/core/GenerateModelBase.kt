package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.validateModel

/**
 * Generates a model data class with the given [properties].
 */
inline fun generateModelSpecBase(
    failureClassName: ClassName,
    modelClassName: ClassName,
    properties: List<ModelProperty>,
    toTypeName: ModelPropertyType.() -> TypeName,
    toValueObjectTypeName: ModelPropertyType.() -> TypeName,
    getModelPropertyFailure: ModelPropertyType.ModelTemplate.() -> ClassName
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
                FunSpec.builder(FunctionNames.of)
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
                            getModelPropertyFailure = getModelPropertyFailure
                        ).build()
                    )
                    .build()
            ).build()
        )
        .build()
}
