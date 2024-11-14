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
                            valueObjectFieldFailure(
                                className = it.type.fieldFailureClassName.simpleName,
                                valueFailureTypeName = it.type.valueFailureType,
                                fieldFailureClassName = fieldFailureClassName
                            )
                        )

                        is ModelPropertyType.ValueObjectOption -> add(
                            valueObjectFieldFailure(
                                className = it.type.fieldFailureClassName.simpleName,
                                valueFailureTypeName = it.type.valueFailureType,
                                fieldFailureClassName = fieldFailureClassName
                            )
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

                        is ModelPropertyType.ModelTemplateOption -> {
                            add(
                                modelTemplateFieldFailure(
                                    name = it.type.fieldFailureClassName.simpleName,
                                    nelFieldFailureType = nelOf(it.type.fieldFailureType),
                                    superInterface = fieldFailureClassName
                                )
                            )
                        }

                        is ModelPropertyType.ModelTemplateList -> {
                            add(
                                modelTemplateFieldFailure(
                                    name = it.type.fieldFailureClassName.simpleName,
                                    nelFieldFailureType = nelOf(nelOf(it.type.fieldFailureType)),
                                    superInterface = fieldFailureClassName
                                )
                            )
                        }

                        is ModelPropertyType.ModelTemplateNel -> {
                            add(
                                modelTemplateFieldFailure(
                                    name = it.type.fieldFailureClassName.simpleName,
                                    nelFieldFailureType = nelOf(nelOf(it.type.fieldFailureType)),
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

private fun valueObjectFieldFailure(
    className: String,
    valueFailureTypeName: TypeName,
    fieldFailureClassName: ClassName
) = TypeSpec.classBuilder(className)
    .addModifiers(KModifier.DATA)
    .primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter("parent", valueFailureTypeName)
            .build()
    )
    .addProperty(
        PropertySpec.builder("parent", valueFailureTypeName)
            .initializer("parent")
            .build()
    )
    .addSuperinterface(fieldFailureClassName)
    .build()

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
