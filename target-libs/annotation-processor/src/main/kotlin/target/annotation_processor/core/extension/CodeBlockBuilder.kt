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

inline fun CodeBlock.Builder.validateModel(
    properties: List<ModelProperty>,
    model: ClassName,
    getModelPropertyFailure: ModelPropertyType.ModelTemplate.() -> ClassName
): CodeBlock.Builder = apply {
    val typedProperties = properties.mapToTyped { if (type.type.isNullable) vName else name }
    validateValueObjects(typedProperties)
    validateModelTemplates(typedProperties)
    returnValidatedModel(model, typedProperties, getModelPropertyFailure)
}

inline fun CodeBlock.Builder.validateModelBuilder(
    properties: List<ModelProperty>,
    model: ClassName,
    getModelPropertyFailure: ModelPropertyType.ModelTemplate.() -> ClassName
): CodeBlock.Builder = apply {
    val typedProperties = properties.mapToTyped { vName }
    validateValueObjects(typedProperties)
    validateModelTemplatesOption(typedProperties)
    returnValidatedModel(model, typedProperties, getModelPropertyFailure)
}

inline fun List<ModelProperty>.mapToTyped(validatedName: ModelProperty.() -> String) = map {
    when (it.type) {
        is ModelPropertyType.Standard -> TypedModelProperty.Standard(it)
        is ModelPropertyType.ValueObject -> TypedModelProperty.ValueObject(it, it.type, it.vName)
        is ModelPropertyType.ModelTemplate -> TypedModelProperty.ModelTemplate(it, it.type, it.validatedName())
    }
}

fun CodeBlock.Builder.validateValueObjects(properties: List<TypedModelProperty>) = properties.forEach {
    if (it is TypedModelProperty.ValueObject) {
        val nonNullableType = it.type.type.copy(nullable = false)
        if (it.type.type.isNullable) {
            addStatement("val ${it.property.vName} = %T.ofNullable(${it.property.name})", nonNullableType)
        } else {
            addStatement("val ${it.property.vName} = %T.of(${it.property.name})", nonNullableType)
        }
    }
}

fun CodeBlock.Builder.validateModelTemplates(properties: List<TypedModelProperty>) = properties.forEach {
    if (it is TypedModelProperty.ModelTemplate) {
        if (it.type.type.isNullable) {
            addStatement("val ${it.property.vName} = ${it.property.name} ?: %T(null)", ClassNames.right)
        }
    }
}

fun CodeBlock.Builder.validateModelTemplatesOption(properties: List<TypedModelProperty>) = properties.forEach {
    if (it is TypedModelProperty.ModelTemplate) {
        if (it.type.type.isNullable) {
            addStatement("val ${it.property.vName} = ${it.property.name}.traverse { it ?: %T(null) }", ClassNames.right)
        } else {
            addStatement("val ${it.property.vName} = ${it.property.name}.traverse(::%M)", MemberNames.identity)
        }
    }
}

inline fun CodeBlock.Builder.returnValidatedModel(
    model: ClassName,
    typedProperties: List<TypedModelProperty>,
    getModelPropertyFailure: ModelPropertyType.ModelTemplate.() -> ClassName
) {
    if (typedProperties.all { it.validatedName == null }) {
        addStatement(
            "return %T(%T(" + typedProperties.joinToString(", ") { it.property.name } + "))",
            ClassNames.right,
            model
        )
    } else {
        val vNamesAndFailures = typedProperties.mapNotNull {
            when (it) {
                is TypedModelProperty.Standard -> null
                is TypedModelProperty.ValueObject -> it.validatedName to it.type.fieldFailureClassName
                is TypedModelProperty.ModelTemplate -> it.validatedName to getModelPropertyFailure(it.type)
            }
        }
        beginControlFlow(
            "return if (${vNamesAndFailures.joinToString(" && ") { "${it.first} is %T" }})",
            *vNamesAndFailures.map { ClassNames.right }.toTypedArray()
        )
        rightConstructorCall(model, typedProperties)
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
    parameters: List<TypedModelProperty>
): CodeBlock.Builder = addStatement(
    "%T(%T(" + parameters.joinToString(", ") { param ->
        param.validatedName?.let { "$it.value" } ?: param.property.name
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
