package target.annotation_processor.core.extension

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import target.annotation_processor.core.domain.*

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

inline fun List<ModelProperty>.mapToTyped(validatedName: ModelProperty.() -> String) = map {
    when (it.type) {
        is ModelPropertyType.Standard -> TypedModelProperty.Standard(it)
        is ModelPropertyType.ValueObjectOption -> TypedModelProperty.ValueObjectOption(
            property = it,
            type = it.type,
            valueObjectType = it.type.valueObjectType,
            fieldFailureClassName = it.type.fieldFailureClassName,
            validatedName = it.vName
        )
        is ModelPropertyType.ValueObject -> TypedModelProperty.ValueObject(it, it.type, it.vName)
        is ModelPropertyType.ModelTemplate -> TypedModelProperty.ModelTemplate(it, it.type, it.validatedName())
    }
}

fun CodeBlock.Builder.validateValueObjects(properties: List<TypedModelProperty>) = properties.forEach {
    when (it) {
        is TypedModelProperty.ModelTemplate,
        is TypedModelProperty.Standard -> {
        }

        is TypedModelProperty.ValueObject -> addValidationStatement(it.property, it.type.type)
        is TypedModelProperty.ValueObjectOption -> addValidationStatement(it.property, it.valueObjectType)
    }
}

fun CodeBlock.Builder.addValidationStatement(property: ModelProperty, valueObjectClassName: ClassName) {
    val nonNullableType = valueObjectClassName.copy(nullable = false)
    if (valueObjectClassName.isNullable) {
        addStatement("val ${property.vName} = %T.ofNullable(${property.name})", nonNullableType)
    } else {
        addStatement("val ${property.vName} = %T.of(${property.name})", nonNullableType)
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
        // TODO: Traverse is deprecated. Replace with:
        //  .fold({ Either.Right(None) }, { it.map(::Some) }) and
        //  .fold({ Either.Right(None) }, { it?.map(::Some) ?: Either.Right(Some(null)) })
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
                is TypedModelProperty.ValueObjectOption -> it.validatedName to it.fieldFailureClassName
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

/**
 * Adds a return statement.
 */
@Suppress("SpellCheckingInspection")
fun CodeBlock.Builder.rtrn(): CodeBlock.Builder = add("return ")
