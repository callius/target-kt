package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.ModelProperty
import target.annotation_processor.core.domain.ModelPropertyType
import target.annotation_processor.core.domain.nelOf

fun generateFieldFailureSpec(
    fieldFailureClassName: ClassName,
    properties: List<ModelProperty>
): TypeSpec {
    return TypeSpec.interfaceBuilder(fieldFailureClassName)
        .addModifiers(KModifier.SEALED)
        .addTypes(
            buildList {
                properties.forEach {
                    when (it.type) {
                        is ModelPropertyType.ValueObject -> add(
                            TypeSpec.classBuilder(it.type.fieldFailureClassName.simpleName)
                                .addModifiers(KModifier.DATA)
                                .primaryConstructor(
                                    FunSpec.constructorBuilder()
                                        .addParameter("parent", it.type.valueFailureType)
                                        .build()
                                )
                                .addProperty(
                                    PropertySpec.builder("parent", it.type.valueFailureType)
                                        .initializer("parent")
                                        .build()
                                )
                                .addSuperinterface(fieldFailureClassName)
                                .build()
                        )

                        is ModelPropertyType.ModelTemplate -> {
                            add(
                                modelTemplateFieldFailure(
                                    name = it.type.fieldFailureClassName.simpleName,
                                    nelFieldFailureType = nelOf(it.type.fieldFailureType),
                                    superInterface = fieldFailureClassName
                                )
                            )
                        }

                        is ModelPropertyType.Standard -> Unit
                    }
                }
            }
        )
        .build()
}

private fun modelTemplateFieldFailure(
    name: String,
    nelFieldFailureType: TypeName,
    superInterface: ClassName,
) = TypeSpec.classBuilder(name)
    .addModifiers(KModifier.DATA)
    .primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter("parent", nelFieldFailureType)
            .build()
    )
    .addProperty(
        PropertySpec.builder("parent", nelFieldFailureType)
            .initializer("parent")
            .build()
    )
    .addSuperinterface(superInterface)
    .build()
