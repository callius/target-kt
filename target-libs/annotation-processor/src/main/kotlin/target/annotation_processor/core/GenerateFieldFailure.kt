package target.annotation_processor.core

import com.squareup.kotlinpoet.*
import target.annotation_processor.core.domain.ModelProperty
import target.annotation_processor.core.domain.ModelPropertyType

fun generateFieldFailureSpec(
    fieldFailureClassName: ClassName,
    requiredFieldFailureClassName: ClassName,
    properties: List<ModelProperty>
): TypeSpec {
    return TypeSpec.interfaceBuilder(fieldFailureClassName)
        .addModifiers(KModifier.SEALED)
        .addTypes(
            buildList {
                properties.forEach {
                    if (it.type is ModelPropertyType.ValueObject) {
                        add(
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
                                .addSuperinterface(
                                    if (it.isExternal)
                                        fieldFailureClassName
                                    else
                                        requiredFieldFailureClassName
                                )
                                .build()
                        )
                    }
                }
            }
        )
        .build()
}
