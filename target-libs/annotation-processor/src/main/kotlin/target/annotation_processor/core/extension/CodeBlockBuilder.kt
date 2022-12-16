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

// TODO: Split this into multiple functions to validate model template parameters. Builder and model/params.
inline fun CodeBlock.Builder.validateModel(
    properties: List<ModelProperty>,
    model: ClassName,
    getModelPropertyFailure: ModelPropertyType.ModelTemplate.() -> ClassName
): CodeBlock.Builder = apply {
    val valueObjects = properties
        .mapNotNull { if (it.type is ModelPropertyType.ValueObject) it to it.type else null }
        .onEach {
            val nonNullableType = it.second.type.copy(nullable = false)
            if (it.second.type.isNullable) {
                addStatement("val ${it.first.vName} = %T.ofNullable(${it.first.name})", nonNullableType)
            } else {
                addStatement("val ${it.first.vName} = %T.of(${it.first.name})", nonNullableType)
            }
        }

    // Finding model templates and adding local values mapping null to right.
    val modelTemplateProperties = properties
        .mapNotNull { if (it.type is ModelPropertyType.ModelTemplate) it to it.type else null }
        .onEach {
            if (it.second.type.isNullable) {
                addStatement("val ${it.first.vName} = ${it.first.name} ?: %T(null)", ClassNames.right)
            }
        }

    if (valueObjects.isEmpty() && modelTemplateProperties.isEmpty()) {
        addStatement("return %T(%T(" + properties.joinToString(", ") { it.name } + "))", ClassNames.right, model)
    } else if (valueObjects.size == 1 && modelTemplateProperties.isEmpty()) {
        // NOTE: Can be replaced with fold or bimap.
        val first = valueObjects.first()
        beginControlFlow(
            "return if (${first.first.vName} is %T)",
            ClassNames.right
        )
        rightConstructorCall(model, properties)
        nextControlFlow("else")
        addStatement("%T(", ClassNames.left)
        indent()
        addStatement("%T(", ClassNames.nonEmptyList)
        indent()
        addStatement(
            "%T((${first.first.vName} as %T).value),",
            first.second.fieldFailureClassName,
            ClassNames.left,
        )
        addStatement("emptyList()")
        unindent()
        addStatement(")")
        unindent()
        addStatement(")")
        endControlFlow()
    } else {
        val vNamesAndFailures = valueObjects.map { it.first.vName to it.second.fieldFailureClassName } +
                modelTemplateProperties.map { it.first.vNameIfNullable() to getModelPropertyFailure(it.second) }
        beginControlFlow(
            "return if (${vNamesAndFailures.joinToString(" && ") { "${it.first} is %T" }})",
            *vNamesAndFailures.map { ClassNames.right }.toTypedArray()
        )
        rightConstructorCall(model, properties)
        nextControlFlow("else")
        addStatement("%T(", ClassNames.left)
        indent()
        beginControlFlow("buildList")
        vNamesAndFailures.forEach {
            addStatement(
                "if (${it.first} is %T) add(%T(${it.first}.value))",
                ClassNames.left,
                it.second
            )
        }
        endControlFlow()
        addStatement(".%M()!!", MemberNames.toNonEmptyListOrNull)
        unindent()
        addStatement(")")
        endControlFlow()
    }
}

fun CodeBlock.Builder.rightConstructorCall(
    className: ClassName,
    parameters: List<ModelProperty>
): CodeBlock.Builder = addStatement(
    "%T(%T(" + parameters.joinToString(", ") {
        when (it.type) {
            is ModelPropertyType.ModelTemplate -> "${if (it.type.type.isNullable) it.vName else it.name}.value"
            is ModelPropertyType.Standard -> it.name
            is ModelPropertyType.ValueObject -> "${it.vName}.value"
        }
    } + "))",
    ClassNames.right,
    className
)

fun CodeBlock.Builder.constructorCall(
    className: ClassName,
    params: List<ModelProperty>,
    checkVName: Boolean = true
): CodeBlock.Builder = add(
    "%T(" + params.joinToString(", ") {
        if (checkVName && it.type is ModelPropertyType.ValueObject) it.vName else it.name
    } + ")\n",
    className
)

fun CodeBlock.Builder.vNameConstructorCall(
    className: ClassName,
    params: List<ModelProperty>
): CodeBlock.Builder = add("%T(" + params.joinToString(", ") { it.vName } + ")\n", className)

/**
 * Adds a return statement.
 */
@Suppress("SpellCheckingInspection")
fun CodeBlock.Builder.rtrn(): CodeBlock.Builder = add("return ")
