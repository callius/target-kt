package target.annotation_processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import target.annotation_processor.core.*
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.*
import target.annotation_processor.core.visitor.CachedTypeReferenceResolverVisitorVoid

class ModelTemplateVisitorPoet(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) :
    CachedTypeReferenceResolverVisitorVoid() {

    companion object {

        private const val modelTemplateSimpleName = "ModelTemplate"
        private const val validationTemplateSimpleName = "ValidationTemplate"
        private const val modelValidationTemplateSimpleName = "ModelValidationTemplate"
        private const val paramsValidationTemplateSimpleName = "ParamsValidationTemplate"
        private const val builderValidationTemplateSimpleName = "BuilderValidationTemplate"

        private const val valueValidatorFailureTypeParameterIndex = 1

        private fun annotationShortNameEqualsModelTemplate(annotation: KSAnnotation) =
            annotation.shortName.asString() == modelTemplateSimpleName
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (classDeclaration.classKind != ClassKind.INTERFACE) {
            logger.error("Only interfaces can be annotated with @$modelTemplateSimpleName", classDeclaration)
            return
        }

        val properties = classDeclaration.getAllProperties().filter { it.validate() }.toList()
        if (properties.isEmpty()) {
            logger.error("Interface annotated with @$modelTemplateSimpleName must declare at least one property.")
            return
        }

        // Getting the package name.
        val packageName = classDeclaration.packageName.asString()

        // Parsing model template annotation.
        val modelTemplate = classDeclaration.annotations.first(::annotationShortNameEqualsModelTemplate)
        val className = modelTemplate.nameArgument()

        // Creating class names.
        val fieldFailureName = className.appendFieldFailure()
        val fieldFailureClassName = ClassName(packageName, fieldFailureName)
        val requiredFieldFailureName = className.appendRequiredFieldFailure()
        val requiredFieldFailureClassName = ClassName(packageName, requiredFieldFailureName)
        val paramsName = className.appendParams()
        val builderName = className.appendBuilder()
        val paramsClassName = ClassName(packageName, paramsName)

        // Generating model properties.
        val modelProperties = generateModelProperties(
            properties = properties,
            addFieldAnnotations = classDeclaration.annotations.findAddFields(),
            fieldFailureClassName
        )
        val (modelValidationFunctions, paramsValidationFunctions, builderValidationFunctions) =
            generateValidationFunctions(classDeclaration, modelProperties)

        // Creating params and builder properties.
        val paramsProperties = modelProperties.filter { it.isNotExternal }

        // Generating files.
        writeFileSpecList(
            Dependencies(false, classDeclaration.containingFile!!),
            generateFileSpec(
                packageName = packageName,
                fileName = fieldFailureName,
                generateRequiredFieldFailureSpec(requiredFieldFailureName),
                generateFieldFailureSpec(
                    fieldFailureClassName = fieldFailureClassName,
                    requiredFieldFailureClassName = requiredFieldFailureClassName,
                    properties = modelProperties
                )
            ),
            generateFileSpec(
                packageName = packageName,
                fileName = className,
                generateModelSpec(
                    failureClassName = fieldFailureClassName,
                    modelClassName = ClassName(packageName, className),
                    properties = modelProperties,
                    validationFunctions = modelValidationFunctions
                )
            ),
            generateFileSpec(
                packageName = packageName,
                fileName = paramsName,
                generateParamsSpec(
                    failureClassName = requiredFieldFailureClassName,
                    modelClassName = paramsClassName,
                    properties = paramsProperties,
                    validationFunctions = paramsValidationFunctions
                )
            ),
            generateFileSpec(
                packageName = packageName,
                fileName = builderName,
                generateBuilderSpec(
                    failureClassName = requiredFieldFailureClassName,
                    builderClassName = ClassName(packageName, builderName),
                    paramsClassName = paramsClassName,
                    paramsProperties = paramsProperties,
                    validationFunctions = builderValidationFunctions
                )
            )
        )
    }

    /**
     * Generates model properties.
     */
    private fun generateModelProperties(
        properties: List<KSPropertyDeclaration>,
        addFieldAnnotations: List<KSAnnotation>,
        fieldFailureClassName: ClassName
    ): List<ModelProperty> {
        return buildList {
            // Adding defined properties.
            properties.forEach { add(it.toModelProperty(fieldFailureClassName)) }

            // Adding properties defined by AddField annotations.
            addFieldAnnotations.forEach { add(it.toModelProperty(fieldFailureClassName)) }
        }
    }

    /**
     * Generates additional validation functions for declared validation templates.
     */
    private fun generateValidationFunctions(
        classDeclaration: KSClassDeclaration,
        classProperties: List<ModelProperty>
    ): Triple<List<ValidationFunction>, List<ValidationFunction>, List<ValidationFunction>> {
        val model = mutableListOf<ValidationFunction>()
        val params = mutableListOf<ValidationFunction>()
        val builder = mutableListOf<ValidationFunction>()

        classDeclaration.declarations.filterIsInstance<KSClassDeclaration>().forEach { subClass ->
            subClass.annotations.firstOrNull {
                when (it.shortName.asString()) {
                    validationTemplateSimpleName,
                    modelValidationTemplateSimpleName,
                    paramsValidationTemplateSimpleName,
                    builderValidationTemplateSimpleName -> true

                    else -> false
                }
            }?.let {
                if (subClass.classKind != ClassKind.INTERFACE || !subClass.isPrivate()) {
                    logger.warn("Target annotated with $validationTemplateSimpleName is not a private interface")
                }

                val validationFunction = generateValidationFunction(it.nameArgument(), classProperties, subClass)
                when (it.shortName.asString()) {
                    validationTemplateSimpleName -> {
                        model.add(validationFunction)
                        params.add(validationFunction)
                        builder.add(validationFunction)
                    }

                    modelValidationTemplateSimpleName -> model.add(validationFunction)
                    paramsValidationTemplateSimpleName -> params.add(validationFunction)
                    builderValidationTemplateSimpleName -> builder.add(validationFunction)
                }
            }
        }

        return Triple(model, params, builder)
    }

    /**
     * Generates a validation function.
     */
    private fun generateValidationFunction(
        name: String,
        classProperties: List<ModelProperty>,
        subClass: KSClassDeclaration
    ): ValidationFunction {
        val subClassProperties = subClass.getAllProperties().filter { it.validate() }
        return ValidationFunction(
            name = name,
            properties = classProperties.map { modelProperty ->
                if (modelProperty.type is ModelPropertyType.ValueObject && subClassProperties.any { it.simpleName.asString() == modelProperty.name }) {
                    modelProperty.copy(
                        type = ModelPropertyType.Standard(
                            type = modelProperty.type.type,
                            typeArguments = emptyList()
                        )
                    )
                } else {
                    modelProperty
                }
            }
        )
    }

    /**
     * Generates a file with the given [typeSpec], adding a comment and proper indent.
     */
    private fun generateFileSpec(packageName: String, fileName: String, vararg typeSpec: TypeSpec): FileSpec {
        return FileSpec.builder(packageName = packageName, fileName = fileName)
            .addGeneratedComment()
            .apply { members.addAll(typeSpec) }
            .indent(Indent.one)
            .build()
    }

    /**
     * Writes the given [fileSpecs] to the [codeGenerator] with the given [dependencies].
     */
    private fun writeFileSpecList(dependencies: Dependencies, vararg fileSpecs: FileSpec) {
        fileSpecs.forEach {
            it.writeTo(codeGenerator, dependencies)
        }
    }

    private fun KSAnnotation.toModelProperty(fieldFailureClassName: ClassName): ModelProperty {
        val addFieldType = typeArgument()
        val propertyName = nameArgument()
        return ModelProperty(
            name = propertyName,
            // NOTE: Add field annotations are not able to capture type arguments.
            type = resolveModelPropertyType(propertyName, addFieldType, emptyList(), fieldFailureClassName),
            isExternal = ignoreArgument(),
        )
    }

    private fun KSPropertyDeclaration.toModelProperty(fieldFailureClassName: ClassName): ModelProperty {
        val simpleNameAsString = simpleName.asString()
        return ModelProperty(
            name = simpleNameAsString,
            type = resolveModelPropertyType(simpleNameAsString, type, fieldFailureClassName),
            isExternal = isExternal(),
        )
    }

    private fun resolveModelPropertyType(
        propertyName: String,
        typeReference: KSTypeReference,
        fieldFailureClassName: ClassName
    ): ModelPropertyType {
        return resolveModelPropertyType(
            propertyName,
            resolveTypeReference(typeReference),
            typeReference.element!!.typeArguments,
            fieldFailureClassName
        )
    }

    private fun resolveModelPropertyType(
        propertyName: String,
        type: KSType,
        typeArguments: List<KSTypeArgument>,
        fieldFailureClassName: ClassName,
    ): ModelPropertyType {
        val typeDeclaration = type.declaration
        val modelTemplate = typeDeclaration.annotations.firstOrNull(::annotationShortNameEqualsModelTemplate)

        return if (modelTemplate == null) {
            if (typeDeclaration is KSClassDeclaration) {
                val valueObjectReference = typeDeclaration.superTypes.firstOrNull {
                    resolveTypeReference(it).declaration.qualifiedName?.asString() == QualifiedNames.valueObject
                }

                if (valueObjectReference == null) {
                    ModelPropertyType.Standard(
                        type = type.toClassName().withNullability(type.nullability),
                        typeArguments = typeArguments.map {
                            when (it.variance) {
                                Variance.STAR -> ModelPropertyTypeArgument.Star
                                Variance.INVARIANT,
                                Variance.COVARIANT,
                                Variance.CONTRAVARIANT -> ModelPropertyTypeArgument.Type(
                                    resolveModelPropertyType(
                                        propertyName,
                                        it.type!!,
                                        fieldFailureClassName
                                    )
                                )
                            }
                        }
                    )
                } else {
                    val typeArgument = valueObjectReference.element?.typeArguments?.firstOrNull()!!
                    val valueObjectType = resolveTypeReference(typeArgument.type!!)
                    val valueFailureType = resolveValueValidatorFailureType(typeDeclaration)

                    ModelPropertyType.ValueObject(
                        type = type.toClassName().withNullability(type.nullability),
                        valueObjectType = valueObjectType.toClassName().withNullability(type.nullability),
                        valueFailureType = valueFailureType,
                        fieldFailureClassName = fieldFailureClassName.nestedClass(
                            propertyName.replaceFirstChar { it.uppercaseChar() }
                        )
                    )
                }
            } else if (typeDeclaration is KSTypeAlias) {
                // NOTE: This should keep the alias's name.
                resolveModelPropertyType(propertyName, typeDeclaration.type, fieldFailureClassName)
            } else {
                ModelPropertyType.Standard(
                    type = type.toClassName().withNullability(type.nullability),
                    typeArguments = typeArguments.map {
                        when (it.variance) {
                            Variance.STAR -> ModelPropertyTypeArgument.Star
                            Variance.INVARIANT,
                            Variance.COVARIANT,
                            Variance.CONTRAVARIANT -> ModelPropertyTypeArgument.Type(
                                resolveModelPropertyType(
                                    propertyName,
                                    it.type!!,
                                    fieldFailureClassName
                                )
                            )
                        }
                    }
                )
            }
        } else {
            val modelName = modelTemplate.nameArgument()
            val upperPropertyName = propertyName.replaceFirstChar { it.uppercaseChar() }
            ModelPropertyType.ModelTemplate(
                type = ClassName(typeDeclaration.packageName.asString(), modelName).withNullability(type.nullability),
                fieldFailureType = ClassName(
                    typeDeclaration.packageName.asString(),
                    modelName.appendFieldFailure()
                ),
                requiredFieldFailureType = ClassName(
                    typeDeclaration.packageName.asString(),
                    modelName.appendRequiredFieldFailure()
                ),
                fieldFailureClassName = fieldFailureClassName.nestedClass(upperPropertyName),
                requiredFieldFailureClassName = fieldFailureClassName.nestedClass("Required$upperPropertyName")
            )
        }
    }

    private fun resolveValueValidatorFailureType(valueObjectDeclaration: KSClassDeclaration): TypeName {
        val companionObject = valueObjectDeclaration.declarations
            .filterIsInstance<KSClassDeclaration>()
            .first { it.isCompanionObject }

        val superTypesAndDeclarations = resolveSuperTypesAndDeclarations(companionObject)
        val valueValidator = superTypesAndDeclarations.firstOrNull { it.second.qualifiedNameEqualsValueValidator() }

        return if (valueValidator == null) {
            superTypesAndDeclarations
                .firstNotNullOfOrNull { resolveValueValidatorFailureType(it, emptyList()) }
                ?: error("Failed to resolve value validator failure type: companion object does not inherit value validator")
        } else {
            val failureTypeReference =
                valueValidator.first.element!!.typeArguments[valueValidatorFailureTypeParameterIndex].type!!
            val failureType = resolveTypeReference(failureTypeReference)
            resolveTypeName(
                failureTypeReference,
                failureType,
                emptyList()
            )
        }
    }

    private fun resolveValueValidatorFailureType(
        typeReferenceAndDeclaration: Pair<KSTypeReference, KSClassDeclaration>,
        children: List<Pair<KSTypeReference, KSClassDeclaration>>
    ): TypeName? {
        val superTypesAndDeclarations = resolveSuperTypesAndDeclarations(typeReferenceAndDeclaration.second)
        val valueValidator = superTypesAndDeclarations.firstOrNull { it.second.qualifiedNameEqualsValueValidator() }

        return if (valueValidator == null) {
            superTypesAndDeclarations.firstNotNullOfOrNull {
                resolveValueValidatorFailureType(
                    it,
                    children.plus(typeReferenceAndDeclaration)
                )
            }
        } else {
            val failureTypeReference = valueValidator.first.element!!.typeArguments[1].type!!
            val failureType = resolveTypeReference(failureTypeReference)
            val failureTypeDeclaration = failureType.declaration
            if (failureTypeDeclaration is KSTypeParameter) {
                val name = failureTypeDeclaration.name
                resolveTypeParameter(
                    typeReferenceAndDeclaration.second.typeParameters.indexOfFirst { it.name.asString() == name.asString() },
                    children.size,
                    children.plus(valueValidator)
                )
            } else {
                resolveTypeName(
                    failureTypeReference,
                    failureType,
                    children.plus(typeReferenceAndDeclaration)
                )
            }
        }
    }

    private fun resolveSuperTypesAndDeclarations(declaration: KSClassDeclaration): Sequence<Pair<KSTypeReference, KSClassDeclaration>> {
        return declaration.superTypes.mapNotNull { superType ->
            resolveTypeReference(superType).declaration.let {
                if (it !is KSClassDeclaration) {
                    null
                } else {
                    superType to it
                }
            }
        }
    }

    private fun resolveTypeParameter(
        typeArgumentIndex: Int,
        childIndex: Int,
        children: List<Pair<KSTypeReference, KSDeclaration>>
    ): TypeName {
        if (childIndex < 0) {
            error("Failed to resolve value validator failure type: implementation was parameterized, but no type arguments were found")
        }

        val referenceAndDeclaration = children[childIndex]
        val typeArgumentReference = referenceAndDeclaration.first.element!!.typeArguments[typeArgumentIndex].type!!
        val typeArgumentType = resolveTypeReference(typeArgumentReference)
        val typeArgumentDeclaration = typeArgumentType.declaration
        return if (typeArgumentDeclaration is KSTypeParameter) {
            val name = typeArgumentDeclaration.name
            resolveTypeParameter(
                children[childIndex - 1].second.typeParameters.indexOfFirst { it.name.asString() == name.asString() },
                childIndex - 1,
                children
            )
        } else {
            resolveTypeName(
                typeArgumentReference,
                typeArgumentType,
                children.subList(0, childIndex + 1)
            )
        }
    }

    private fun KSClassDeclaration.qualifiedNameEqualsValueValidator(): Boolean {
        return qualifiedName?.asString() == QualifiedNames.valueValidator
    }

    private fun resolveTypeName(
        typeReference: KSTypeReference,
        type: KSType,
        parents: List<Pair<KSTypeReference, KSDeclaration>>
    ): TypeName {
        val typeArguments = typeReference.element!!.typeArguments
        return if (typeArguments.isEmpty()) {
            type.toClassName()
        } else {
            val parentsPlusThis = parents.plus(typeReference to type.declaration)
            type.toClassName().parameterizedBy(
                List(typeArguments.size) { index ->
                    resolveTypeParameter(index, parentsPlusThis.lastIndex, parentsPlusThis)
                }
            )
        }
    }
}
