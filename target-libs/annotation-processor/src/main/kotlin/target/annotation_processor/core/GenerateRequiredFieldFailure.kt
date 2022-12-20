package target.annotation_processor.core

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

fun generateRequiredFieldFailureSpec(requiredFieldFailureName: String): TypeSpec {
    return TypeSpec.interfaceBuilder(requiredFieldFailureName)
        .addModifiers(KModifier.SEALED)
        .build()
}
