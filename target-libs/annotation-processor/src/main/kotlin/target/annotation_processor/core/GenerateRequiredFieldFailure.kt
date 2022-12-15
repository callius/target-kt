package target.annotation_processor.core

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

fun generateRequiredFieldFailureSpec(requiredFieldFailureName: String, fieldFailureClassName: ClassName): TypeSpec {
    return TypeSpec.interfaceBuilder(requiredFieldFailureName)
        .addModifiers(KModifier.SEALED)
        .addSuperinterface(fieldFailureClassName)
        .build()
}
