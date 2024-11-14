package target.annotation_processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.addGeneratedComment
import target.annotation_processor.core.extension.appendFieldFailure
import target.annotation_processor.core.extension.toClassNameWithNullability
import target.annotation_processor.core.extension.withNullability
import target.annotation_processor.core.generateCompanionOfSpec
import target.annotation_processor.core.generateCompanionOnlySpec
import target.annotation_processor.core.generateFieldFailureSpec
import target.annotation_processor.core.visitor.CachedTypeReferenceResolverVisitorVoid

class ValidatableVisitorPoet(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) :
    CachedTypeReferenceResolverVisitorVoid() {

    companion object {

        private const val VALIDATABLE_SIMPLE_NAME = "Validatable"

        private const val VALUE_VALIDATOR_FAILURE_TYPE_PARAMETER_INDEX = 1

        private fun annotationShortNameEqualsValidatable(annotation: KSAnnotation) =
            annotation.shortName.asString() == VALIDATABLE_SIMPLE_NAME
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (!classDeclaration.modifiers.contains(Modifier.DATA)) {
            logger.error("Only data classes can be annotated with @$VALIDATABLE_SIMPLE_NAME", classDeclaration)
            return
        }

        val properties = classDeclaration.getAllProperties().filter { it.validate() }.toList()
        if (properties.isEmpty()) {
            logger.error("Data class annotated with @$VALIDATABLE_SIMPLE_NAME must declare at least one property.")
            return
        }

        val companionObjectDeclaration = classDeclaration.declarations.firstOrNull {
            it is KSClassDeclaration && it.isCompanionObject
        }
        if (companionObjectDeclaration == null) {
            logger.error("Data class annotated with @$VALIDATABLE_SIMPLE_NAME must declare a companion object.")
            return
        }

        // Getting the package name.
        val packageName = classDeclaration.packageName.asString()

        // Parsing model template annotation.
        val modelName = classDeclaration.simpleName.asString()
        val modelClassName = ClassName(packageName, modelName)

        // Creating companion object class name.
        val companionObjectClassName =
            ClassName(packageName, modelName, companionObjectDeclaration.simpleName.asString())

        // Creating failure class names.
        val fieldFailureName = modelName.appendFieldFailure()
        val fieldFailureClassName = ClassName(packageName, fieldFailureName)

        // Generating model properties.
        val modelProperties = generateModelProperties(
            properties = properties,
            fieldFailureClassName = fieldFailureClassName
        )

        val shouldGenerateOnly = modelProperties.any { it.type.type == ClassNames.option }

        // Generating files.
        writeFileSpecList(
            Dependencies(false, classDeclaration.containingFile!!),
            buildList {
                add(
                    generateFileSpec(
                        packageName = packageName,
                        fileName = fieldFailureName,
                        generateFieldFailureSpec(
                            fieldFailureClassName = fieldFailureClassName,
                            properties = modelProperties
                        )
                    )
                )
                add(
                    generateFileSpec(
                        packageName = packageName,
                        fileName = "${modelName}CompanionOf",
                        generateCompanionOfSpec(
                            failureClassName = fieldFailureClassName,
                            modelClassName = modelClassName,
                            companionObjectClassName = companionObjectClassName,
                            properties = modelProperties
                        )
                    )
                )
                if (shouldGenerateOnly) {
                    add(
                        generateFileSpec(
                            packageName = packageName,
                            fileName = "${modelName}CompanionOnly",
                            generateCompanionOnlySpec(
                                modelClassName = modelClassName,
                                companionObjectClassName = companionObjectClassName,
                                properties = modelProperties
                            )
                        )
                    )
                }
            }
        )
    }

    /**
     * Generates model properties.
     */
    private fun generateModelProperties(
        properties: List<KSPropertyDeclaration>,
        fieldFailureClassName: ClassName
    ): List<ModelProperty> {
        return properties.map { it.toModelProperty(fieldFailureClassName) }
    }

    /**
     * Generates a file with the given [specs], adding a comment and proper indent.
     */
    private fun generateFileSpec(packageName: String, fileName: String, vararg specs: Any): FileSpec {
        return FileSpec.builder(packageName = packageName, fileName = fileName)
            .addGeneratedComment()
            .apply { members.addAll(specs) }
            .indent(Indent.ONE)
            .build()
    }

    /**
     * Writes the given [fileSpecs] to the [codeGenerator] with the given [dependencies].
     */
    private fun writeFileSpecList(dependencies: Dependencies, fileSpecs: List<FileSpec>) {
        fileSpecs.forEach {
            it.writeTo(codeGenerator, dependencies)
        }
    }

    private fun KSPropertyDeclaration.toModelProperty(fieldFailureClassName: ClassName): ModelProperty {
        val simpleNameAsString = simpleName.asString()
        return ModelProperty(
            name = simpleNameAsString,
            type = resolveModelPropertyType(simpleNameAsString, type, fieldFailureClassName),
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
        val modelTemplate = typeDeclaration.annotations.firstOrNull(::annotationShortNameEqualsValidatable)

        // Finding model template option.
        if (modelTemplate != null) {
            val modelTypeName = type.toClassName()
            val upperPropertyName = propertyName.replaceFirstChar { it.uppercaseChar() }
            return ModelPropertyType.ModelTemplate(
                type = modelTypeName.withNullability(type.nullability),
                fieldFailureType = ClassName(
                    modelTypeName.packageName,
                    modelTypeName.simpleName.appendFieldFailure()
                ),
                fieldFailureClassName = fieldFailureClassName.nestedClass(upperPropertyName)
            )
        }

        return when (typeDeclaration) {
            is KSClassDeclaration -> resolveModelPropertyTypeClassDeclaration(
                propertyName = propertyName,
                type = type,
                typeArguments = typeArguments,
                typeDeclaration = typeDeclaration,
                fieldFailureClassName = fieldFailureClassName
            )

            is KSTypeAlias -> resolveModelPropertyType(
                propertyName = propertyName,
                typeReference = typeDeclaration.type,
                fieldFailureClassName = fieldFailureClassName
            )

            else -> ModelPropertyType.Standard(
                type = type.toClassName().withNullability(type.nullability),
                typeArguments = typeArguments.map {
                    when (it.variance) {
                        Variance.STAR -> ModelPropertyTypeArgument.Star
                        Variance.INVARIANT,
                        Variance.COVARIANT,
                        Variance.CONTRAVARIANT -> ModelPropertyTypeArgument.Type(
                            resolveModelPropertyType(
                                propertyName = propertyName,
                                typeReference = it.type!!,
                                fieldFailureClassName = fieldFailureClassName
                            )
                        )
                    }
                }
            )
        }
    }

    private fun resolveModelPropertyTypeClassDeclaration(
        propertyName: String,
        type: KSType,
        typeArguments: List<KSTypeArgument>,
        typeDeclaration: KSClassDeclaration,
        fieldFailureClassName: ClassName
    ): ModelPropertyType {
        if (typeDeclaration.toClassName().canonicalName == QualifiedNames.ARROW_OPTION) {
            // Finding model template option.
            val validatableTypeArgument = typeArguments.firstNotNullOfOrNull { arg ->
                val argTypeRef = arg.type ?: return@firstNotNullOfOrNull null
                resolveTypeReference(argTypeRef).takeIf {
                    it.declaration.annotations.any(::annotationShortNameEqualsValidatable)
                }
            }
            if (validatableTypeArgument != null) {
                val modelTypeName = validatableTypeArgument.toClassNameWithNullability()
                val upperPropertyName = propertyName.replaceFirstChar { it.uppercaseChar() }
                return ModelPropertyType.ModelTemplateOption(
                    type = ClassNames.option.withNullability(type.nullability),
                    modelType = modelTypeName,
                    fieldFailureType = ClassName(
                        modelTypeName.packageName,
                        modelTypeName.simpleName.appendFieldFailure()
                    ),
                    fieldFailureClassName = fieldFailureClassName.nestedClass(upperPropertyName)
                )
            }

            // Finding value object option.
            val valueObjectReferencePair = typeArguments.firstNotNullOfOrNull { arg ->
                val argTypeRef = arg.type ?: return@firstNotNullOfOrNull null
                val (argType, argTypeDec) = resolveTypeReferenceAndClassDeclaration(argTypeRef)
                    ?: return@firstNotNullOfOrNull null

                argTypeDec.superTypes.firstOrNull {
                    resolveTypeReference(it).declaration.qualifiedName?.asString() == QualifiedNames.VALUE_OBJECT
                }?.let {
                    argType to it
                }
            }
            if (valueObjectReferencePair != null) {
                val typeArgument = valueObjectReferencePair.second.element?.typeArguments?.firstOrNull()!!
                val valueObjectValueType = resolveTypeReference(typeArgument.type!!)
                val valueFailureType = resolveValueValidatorFailureType(
                    valueObjectReferencePair.first.declaration as KSClassDeclaration
                )

                return ModelPropertyType.ValueObjectOption(
                    type = ClassNames.option.withNullability(type.nullability),
                    valueObjectType = valueObjectReferencePair.first.toClassNameWithNullability(),
                    valueObjectValueType = valueObjectValueType.toClassName().withNullability(
                        valueObjectReferencePair.first.nullability
                    ),
                    valueFailureType = valueFailureType,
                    fieldFailureClassName = fieldFailureClassName.nestedClass(
                        propertyName.replaceFirstChar { it.uppercaseChar() }
                    )
                )
            }
        }

        if (typeDeclaration.toClassName().canonicalName == QualifiedNames.ARROW_NON_EMPTY_LIST) {
            // Finding model template nel.
            val validatableTypeArgument = typeArguments.firstNotNullOfOrNull { arg ->
                val argTypeRef = arg.type ?: return@firstNotNullOfOrNull null
                resolveTypeReference(argTypeRef).takeIf {
                    it.declaration.annotations.any(::annotationShortNameEqualsValidatable)
                }
            }
            if (validatableTypeArgument != null) {
                val modelTypeName = validatableTypeArgument.toClassNameWithNullability()
                val upperPropertyName = propertyName.replaceFirstChar { it.uppercaseChar() }
                return ModelPropertyType.ModelTemplateNel(
                    type = ClassNames.option.withNullability(type.nullability),
                    modelType = modelTypeName,
                    fieldFailureType = ClassName(
                        modelTypeName.packageName,
                        modelTypeName.simpleName.appendFieldFailure()
                    ),
                    fieldFailureClassName = fieldFailureClassName.nestedClass(upperPropertyName)
                )
            }
        }

        if (typeDeclaration.toClassName().canonicalName == QualifiedNames.LIST) {
            // Finding model template list.
            val validatableTypeArgument = typeArguments.firstNotNullOfOrNull { arg ->
                val argTypeRef = arg.type ?: return@firstNotNullOfOrNull null
                resolveTypeReference(argTypeRef).takeIf {
                    it.declaration.annotations.any(::annotationShortNameEqualsValidatable)
                }
            }
            if (validatableTypeArgument != null) {
                val modelTypeName = validatableTypeArgument.toClassNameWithNullability()
                val upperPropertyName = propertyName.replaceFirstChar { it.uppercaseChar() }
                return ModelPropertyType.ModelTemplateList(
                    type = ClassNames.option.withNullability(type.nullability),
                    modelType = modelTypeName,
                    fieldFailureType = ClassName(
                        modelTypeName.packageName,
                        modelTypeName.simpleName.appendFieldFailure()
                    ),
                    fieldFailureClassName = fieldFailureClassName.nestedClass(upperPropertyName)
                )
            }
        }

        val valueObjectReference = typeDeclaration.superTypes.firstOrNull {
            resolveTypeReference(it).declaration.qualifiedName?.asString() == QualifiedNames.VALUE_OBJECT
        }
        if (valueObjectReference != null) {
            val typeArgument = valueObjectReference.element?.typeArguments?.firstOrNull()!!
            val valueObjectValueType = resolveTypeReference(typeArgument.type!!)
            val valueFailureType = resolveValueValidatorFailureType(typeDeclaration)

            return ModelPropertyType.ValueObject(
                type = type.toClassNameWithNullability(),
                valueObjectValueType = valueObjectValueType.toClassName().withNullability(type.nullability),
                valueFailureType = valueFailureType,
                fieldFailureClassName = fieldFailureClassName.nestedClass(
                    propertyName.replaceFirstChar { it.uppercaseChar() }
                )
            )
        }

        return ModelPropertyType.Standard(
            type = type.toClassName().withNullability(type.nullability),
            typeArguments = typeArguments.map {
                when (it.variance) {
                    Variance.STAR -> ModelPropertyTypeArgument.Star
                    Variance.INVARIANT,
                    Variance.COVARIANT,
                    Variance.CONTRAVARIANT -> ModelPropertyTypeArgument.Type(
                        resolveModelPropertyType(
                            propertyName = propertyName,
                            typeReference = it.type!!,
                            fieldFailureClassName = fieldFailureClassName
                        )
                    )
                }
            }
        )
    }

    private fun resolveTypeReferenceAndClassDeclaration(reference: KSTypeReference): Pair<KSType, KSClassDeclaration>? {
        val type = resolveTypeReference(reference)
        return when (val it = type.declaration) {
            is KSClassDeclaration -> type to it
            is KSTypeAlias -> resolveTypeReferenceAndClassDeclaration(it.type)
            else -> null
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
                valueValidator.first.element!!.typeArguments[VALUE_VALIDATOR_FAILURE_TYPE_PARAMETER_INDEX].type!!
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
        return qualifiedName?.asString() == QualifiedNames.VALUE_VALIDATOR
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
