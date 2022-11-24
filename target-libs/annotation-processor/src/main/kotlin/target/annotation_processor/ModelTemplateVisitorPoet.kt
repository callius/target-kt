package target.annotation_processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import target.annotation_processor.core.domain.*
import target.annotation_processor.core.extension.*
import target.annotation_processor.core.generateBuilderSpec
import target.annotation_processor.core.generateModelSpec
import target.annotation_processor.core.generateParamsSpec
import target.annotation_processor.core.visitor.CachedTypeReferenceResolverVisitorVoid

class ModelTemplateVisitorPoet(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) :
    CachedTypeReferenceResolverVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (classDeclaration.classKind != ClassKind.INTERFACE) {
            logger.error("Only interfaces can be annotated with @ModelTemplate", classDeclaration)
            return
        }

        val properties = classDeclaration.getAllProperties().filter { it.validate() }.toList()
        if (properties.isEmpty()) {
            logger.error("Interface annotated with @ModelTemplate must declare at least one property.")
            return
        }

        // Getting the package name.
        val packageName = classDeclaration.packageName.asString()

        // Parsing model template annotation.
        val modelTemplate = classDeclaration.annotations.first { it.shortName.asString() == "ModelTemplate" }
        val className = modelTemplate.nameArgument()
        val failureClass = modelTemplate.failureArgument()
        val idField = modelTemplate.idFieldArgument()
        val customId = modelTemplate.customIdArgument()

        // Generating model properties.
        val modelProperties = generateModelProperties(
            properties = properties,
            addFieldAnnotations = classDeclaration.annotations.findAddFields(),
            addFieldId = if (customId) null else idField
        )

        // Creating params and builder properties.
        val paramsProperties = modelProperties.filter { it.isNotExternal }

        // Creating class names.
        val paramsName = className.appendParams()
        val builderName = className.appendBuilder()
        val paramsClassName = ClassName(packageName, paramsName)

        // Generating files.
        writeFileSpecList(
            Dependencies(false, classDeclaration.containingFile!!),
            generateFileSpec(
                packageName = packageName,
                fileName = className,
                generateModelSpec(
                    failureClassName = failureClass.toClassName(),
                    modelClassName = ClassName(packageName, className),
                    properties = modelProperties
                ),
            ),
            generateFileSpec(
                packageName = packageName,
                fileName = paramsName,
                generateParamsSpec(
                    failureClassName = failureClass.toClassName(),
                    modelClassName = paramsClassName,
                    properties = paramsProperties
                )
            ),
            generateFileSpec(
                packageName = packageName,
                fileName = builderName,
                generateBuilderSpec(
                    failureClassName = failureClass.toClassName(),
                    builderClassName = ClassName(packageName, builderName),
                    paramsClassName = paramsClassName,
                    paramsProperties = paramsProperties,
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
        addFieldId: KSAnnotation?
    ): List<ModelProperty> {
        return mutableListOf<ModelProperty>().apply {
            // Adding id property.
            if (addFieldId != null) {
                add(addFieldId.toModelProperty())
            }

            // Adding defined properties.
            properties.forEach { add(it.toModelProperty()) }

            // Adding properties defined by AddField annotations.
            addFieldAnnotations.forEach { add(it.toModelProperty()) }
        }
    }

    /**
     * Generates a file with the given [typeSpec], adding a comment and proper indent.
     */
    private fun generateFileSpec(packageName: String, fileName: String, typeSpec: TypeSpec): FileSpec {
        return FileSpec.builder(packageName = packageName, fileName = fileName)
            .addGeneratedComment()
            .addType(typeSpec)
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

    private fun KSAnnotation.toModelProperty(): ModelProperty {
        val addFieldType = typeArgument()
        return ModelProperty(
            name = nameArgument(),
            // NOTE: Add field annotations are not able to capture type arguments.
            type = resolveModelPropertyType(addFieldType, emptyList()),
            isExternal = ignoreArgument(),
        )
    }

    private fun KSPropertyDeclaration.toModelProperty(): ModelProperty {
        return ModelProperty(
            name = simpleName.asString(),
            type = resolveModelPropertyType(type),
            isExternal = isExternal(),
        )
    }

    private fun resolveModelPropertyType(typeReference: KSTypeReference): ModelPropertyType {
        return resolveModelPropertyType(resolveTypeReference(typeReference), typeReference.element!!.typeArguments)
    }

    private fun resolveModelPropertyType(type: KSType, typeArguments: List<KSTypeArgument>): ModelPropertyType {
        val modelTemplate = type.declaration.annotations.firstOrNull { it.shortName.asString() == "ModelTemplate" }

        return if (modelTemplate == null) {
            if (type.declaration is KSClassDeclaration) {
                val valueObjectReference = (type.declaration as KSClassDeclaration).superTypes.firstOrNull {
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
                                Variance.CONTRAVARIANT -> ModelPropertyTypeArgument.Type(resolveModelPropertyType(it.type!!))
                            }
                        }
                    )
                } else {
                    val typeArgument = valueObjectReference.element?.typeArguments?.firstOrNull()!!
                    val valueObjectType = resolveTypeReference(typeArgument.type!!)

                    ModelPropertyType.ValueObject(
                        type = type.toClassName().withNullability(type.nullability),
                        valueObjectType = valueObjectType.toClassName().withNullability(type.nullability)
                    )
                }
            } else if (type.declaration is KSTypeAlias) {
                // NOTE: This should keep the alias's name.
                resolveModelPropertyType((type.declaration as KSTypeAlias).type)
            } else {
                ModelPropertyType.Standard(
                    type = type.toClassName().withNullability(type.nullability),
                    typeArguments = typeArguments.map {
                        when (it.variance) {
                            Variance.STAR -> ModelPropertyTypeArgument.Star
                            Variance.INVARIANT,
                            Variance.COVARIANT,
                            Variance.CONTRAVARIANT -> ModelPropertyTypeArgument.Type(resolveModelPropertyType(it.type!!))
                        }
                    }
                )
            }
        } else {
            ModelPropertyType.ModelTemplate(
                ClassName(
                    type.declaration.packageName.asString(),
                    modelTemplate.nameArgument()
                ).withNullability(type.nullability)
            )
        }
    }
}
