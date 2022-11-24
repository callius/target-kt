package target.annotation_processor.core.extension

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import target.annotation_processor.core.domain.*

/**
 * Applies a zip function for the given Option [params] with the given [block] body.
 */
fun CodeBlock.Builder.zipOptionParams(
    params: List<ModelProperty>,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    if (params.isEmpty()) {
        add("%T(", ClassNames.some)
        block()
        add(")")
    } else {
        val param = params.first()
        addOptionPropertyReceiver(param) {
            if (params.size == 1) {
                map(param.vName, block)
            } else {
                flatMap(param.vName) {
                    zipOptionParams(params.drop(1), block)
                }
            }
        }
    }
}

/**
 * Adds the receiver for the given Option [param]. If the [param] is a builder,
 * the receiver is the result of the flat-mapped built value.
 */
inline fun CodeBlock.Builder.addOptionPropertyReceiver(
    param: ModelProperty,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    when (val optionType =
        ((param.type as ModelPropertyType.Standard).typeArguments.first() as ModelPropertyTypeArgument.Type).parent) {
        is ModelPropertyType.ModelTemplate -> {
            val vName1 = "${param.vName}1"
            flatMap(param.name, vName1) {
                if (optionType.type.isNullable) {
                    add("($vName1?.build() ?: %T(null))", ClassNames.some)
                } else {
                    add("$vName1.build()")
                }
                block()
            }
        }
        is ModelPropertyType.Standard,
        is ModelPropertyType.ValueObject -> add(param.name).block()
    }
}

/**
 * Applies a zip function for the given value object [properties], calling the value validator.
 */
inline fun CodeBlock.Builder.ofAndZip(
    properties: List<ModelProperty>,
    failure: ClassName,
    crossinline block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    when {
        properties.isEmpty() -> {
            add("%T(", ClassNames.right)
            block()
            add(")")
        }
        properties.size == 1 -> {
            if (failure.isValueFailure()) {
                ofAndMap(properties.first(), block)
            } else {
                ofAndBimap(
                    properties.first(),
                    { add(failure.constructorReference()) },
                    { block() }
                )
            }
        }
        else -> {
            ofAndFlatMap(properties.dropLast(1)) {
                ofAndMap(properties.last()) {
                    block()
                }
            }
            if (!failure.isValueFailure()) {
                mapLeftFailure(failure)
            }
        }
    }
}

/**
 * Calls the value validator for the given value object [property], then calls 'map' with the given [block] body.
 */
inline fun CodeBlock.Builder.ofAndMap(
    property: ModelProperty,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    of(property)
    beginControlFlow(
        ".map·{ ${property.vName} ->",
    )
    block()
    endControlFlow()
}

/**
 * Calls the value validator for the given value object [property], checking for nullability.
 */
fun CodeBlock.Builder.of(property: ModelProperty): CodeBlock.Builder = apply {
    val nonNullableType = property.type.type.copy(nullable = false)
    if (property.type.type.isNullable) {
        add("%T.ofNullable(${property.name})", nonNullableType)
    } else {
        add("%T.of(${property.name})", nonNullableType)
    }
}

/**
 * Calls the value validator for the given value object [param], then calls 'bimap' with [ifLeft], and [ifRight].
 */
inline fun CodeBlock.Builder.ofAndBimap(
    param: ModelProperty,
    ifLeft: CodeBlock.Builder.() -> CodeBlock.Builder,
    ifRight: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    add(
        "%T.of(${param.name}).bimap(",
        param.type
    )
    ifLeft()
    beginControlFlow(", { ${param.vName} ->")
    ifRight()
    endFlowControl(false)
    add(")")
}

/**
 * Ends the control flow with an optional new line.
 */
fun CodeBlock.Builder.endFlowControl(endNewLine: Boolean): CodeBlock.Builder = apply {
    if (endNewLine) {
        endControlFlow()
    } else {
        unindent()
        add("}")
    }
}

/**
 * Recursively calls the value validator, then 'flatMap' for the given value object [properties],
 * applying the [block] to the innermost body.
 */
fun CodeBlock.Builder.ofAndFlatMap(
    properties: List<ModelProperty>,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    if (properties.isEmpty()) {
        block()
    } else {
        ofAndFlatMap(properties.dropLast(1)) {
            ofAndFlatMap(properties.last(), properties.size != 1, block)
        }
    }
}

/**
 * Calls the value validator for the given value object [property], then calls 'flatMap' with the given [block] body.
 */
inline fun CodeBlock.Builder.ofAndFlatMap(
    property: ModelProperty,
    endNewLine: Boolean = true,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    of(property)
    beginControlFlow(
        ".%M·{ ${property.vName} ->",
        MemberNames.flatMap
    )
    block()
    endFlowControl(endNewLine)
}

/**
 * Recursively calls 'flatMap' for the given [properties], applying the [block] to the innermost body.
 */
fun CodeBlock.Builder.flatMap(
    properties: List<ModelProperty>,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    if (properties.isEmpty()) {
        block()
    } else {
        flatMap(properties.dropLast(1)) {
            flatMap(properties.last(), properties.size != 1, block)
        }
    }
}

/**
 * Calls 'flatMap' for the given [property] with the given [block] body.
 */
inline fun CodeBlock.Builder.flatMap(
    property: ModelProperty,
    endNewLine: Boolean = true,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    beginControlFlow(
        "${property.name}.%M·{ ${property.vName} ->",
        MemberNames.flatMap
    )
    block()
    endFlowControl(endNewLine)
}

/**
 * Calls 'flatMap' on the given [receiver] with the given [block] body containing the scoped [parameterName].
 */
inline fun CodeBlock.Builder.flatMap(
    receiver: String,
    parameterName: String,
    endNewLine: Boolean = true,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    beginControlFlow(
        "$receiver.%M·{ $parameterName ->",
        MemberNames.flatMap
    )
    block()
    endFlowControl(endNewLine)
}

/**
 * Calls 'flatMap' with the given [block] body containing the scoped [parameterName].
 */
inline fun CodeBlock.Builder.flatMap(
    parameterName: String,
    endNewLine: Boolean = true,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    beginControlFlow(
        ".%M·{ $parameterName ->",
        MemberNames.flatMap
    )
    block()
    endFlowControl(endNewLine)
}

/**
 * Calls 'map' for the given [property] with the given [block] body.
 */
inline fun CodeBlock.Builder.map(
    property: ModelProperty,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    beginControlFlow(
        "${property.name}.map·{ ${property.vName} ->",
    )
    block()
    endControlFlow()
}

/**
 * Calls '.map' with the given [block] body containing the scoped [parameterName].
 */
inline fun CodeBlock.Builder.map(
    parameterName: String,
    block: CodeBlock.Builder.() -> CodeBlock.Builder
): CodeBlock.Builder = apply {
    beginControlFlow(".map·{ $parameterName ->")
    block()
    endControlFlow()
}

/**
 * Calls 'mapLeft' with a reference to the constructor of the given [className].
 */
fun CodeBlock.Builder.mapLeftFailure(className: ClassName): CodeBlock.Builder = apply {
    add(".mapLeft(")
    add(className.constructorReference())
    add(")")
}

fun CodeBlock.Builder.ofAndZipConstructor(
    params: List<ModelProperty>,
    failure: ClassName,
    model: ClassName
): CodeBlock.Builder = ofAndZip(params.filter { it.type is ModelPropertyType.ValueObject }, failure) {
    constructorCall(model, params = params)
}

fun CodeBlock.Builder.constructorCall(
    className: ClassName,
    params: List<ModelProperty>,
    checkVName: Boolean = true
): CodeBlock.Builder = add("%T(%L)\n", className, params.joinToString(", ") {
    if (checkVName && it.type is ModelPropertyType.ValueObject) it.vName else it.name
})

fun CodeBlock.Builder.vNameConstructorCall(
    className: ClassName,
    params: List<ModelProperty>,
): CodeBlock.Builder = add("%T(%L)\n", className, params.joinToString(", ") { it.vName })

/**
 * Adds a return statement.
 */
@Suppress("SpellCheckingInspection")
fun CodeBlock.Builder.rtrn(): CodeBlock.Builder = add("return ")
