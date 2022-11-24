package target.annotation_processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import target.annotation_processor.core.domain.Indent
import target.annotation_processor.core.domain.QualifiedNames
import target.annotation_processor.core.extension.failureArgument
import target.annotation_processor.core.extension.nameArgument
import target.annotation_processor.core.visitor.IndentedStringBuilderVisitorVoid
import java.io.OutputStream

class ModelTemplateVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : IndentedStringBuilderVisitorVoid() {

    private val imports = mutableSetOf<String>()

    private val typeCache = mutableMapOf<KSTypeReference, KSType>()

    private fun OutputStream.write(input: String) {
        write(input.toByteArray())
    }

    private fun OutputStream.writeLine() {
        write("\n")
    }

    private fun OutputStream.writeLine(input: String) {
        write(input)
        writeLine()
    }

    private fun createFile(
        dependencies: Dependencies,
        packageName: String,
        fileName: String,
        imports: Set<String>,
        content: String
    ) {
        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = fileName
        ).run {
            writeLine("// Generated code. Do not modify by hand.")
            writeLine("package $packageName")
            writeLine()
            imports.forEach { qualifiedName ->
                writeLine("import $qualifiedName")
            }
            writeLine()
            write(content)
            close()
        }
    }

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

        // Parsing model template annotation.
        val annotation = classDeclaration.annotations.first { it.shortName.asString() == "ModelTemplate" }
        val className = annotation.nameArgument()
        val failureClass = annotation.failureArgument()

        // Generating model class.
        createDataClass(className, { appendConstructorParameters(properties) }) {
            appendCompanionObject {
                appendOfFunction(className, failureClass, properties, Indent.two)
            }
        }

        // Creating a new file containing the generated code.
        createFile(
            dependencies = Dependencies(false, classDeclaration.containingFile!!),
            packageName = classDeclaration.packageName.asString(),
            fileName = className,
            imports = imports.toSortedSet(),
            content = build()
        )
    }

    private fun appendOfFunction(
        className: String,
        failureType: KSType,
        properties: List<KSPropertyDeclaration>,
        indent: String
    ) {
        val failureDeclaration =
            if (failureType.declaration.qualifiedName?.asString() == QualifiedNames.valueFailure) {
                "ValueFailure<*>"
            } else if (failureType.arguments.isNotEmpty()) {
                logger.error("Failure class cannot contain type parameters")
                return
            } else {
                failureType.declaration.simpleName.asString()
            }

        // Adding either and failure imports.
        imports.add(QualifiedNames.arrowEither)
        addImport(failureType.declaration.qualifiedName!!.asString())

        // Building of function.
        appendLine()
        appendLine("${indent}fun of(")
        appendOfFunctionParameters(properties, indent + Indent.one)
        append("$indent): Either<$failureDeclaration, $className> = ")
        appendZip(className, properties, indent)
    }

    private fun appendZip(className: String, properties: List<KSPropertyDeclaration>, indent: String) {
        imports.add(QualifiedNames.arrowFlatMap)
        appendZipValueObjects(properties.filter { isValueObject(it.type) }, indent, true) { newIndent ->
            append("$newIndent$className(")
            properties.forEachIndexed { index, property ->
                if (isValueObject(property.type)) {
                    append(property.simpleName.asString().vName())
                } else {
                    append(property.simpleName.asString())
                }
                if (index < properties.lastIndex) {
                    append(", ")
                }
            }
            appendLine(")")
        }
    }

    private fun appendZipValueObjects(
        properties: List<KSPropertyDeclaration>,
        indent: String,
        skipIndent: Boolean,
        next: (String) -> Unit
    ) {
        if (properties.isEmpty()) {
            next(indent + Indent.one)
            return
        }

        val property = properties.last()
        val propertyName = property.simpleName.asString()
        val vName = propertyName.vName()
        val type = resolveTypeReference(property.type)
        val typeName = type.declaration.simpleName.asString()

        if (!skipIndent) {
            append(indent)
        }

        if (type.nullability == Nullability.NULLABLE) {
            append("$typeName.ofNullable($propertyName)")
        } else {
            append("$typeName.of($propertyName)")
        }

        if (properties.size == 1) {
            appendLine(".map { $vName ->")
            next(indent + Indent.one)
        } else {
            appendLine(".flatMap { $vName ->")
            appendZipValueObjects(properties.dropLast(1), indent + Indent.one, false, next)
        }

        appendLine("$indent}")
    }

    private fun appendOfFunctionParameters(properties: List<KSPropertyDeclaration>, indent: String) {
        val iterator = properties.iterator()
        while (iterator.hasNext()) {
            val property = iterator.next()
            val simpleName = property.simpleName.asString()

            append("$indent$simpleName: ")
            appendUnwrappedTypeReference(property.type)
            appendLine((if (iterator.hasNext()) "," else ""))
        }
    }

    private fun appendConstructorParameters(properties: List<KSPropertyDeclaration>) {
        val iterator = properties.iterator()
        while (iterator.hasNext()) {
            val property = iterator.next()
            val simpleName = property.simpleName.asString()

            // Appending constructor parameter.
            appendIndent("val $simpleName: ")
            appendTypeReference(property.type)

            if (iterator.hasNext()) {
                appendLine(",")
            } else {
                appendLine()
            }
        }
    }

    private fun appendTypeReference(typeReference: KSTypeReference) {
        val type = resolveTypeReference(typeReference)

        // Checking model type.
        val modelTemplate = type.declaration.annotations.firstOrNull { it.shortName.asString() == "ModelTemplate" }
        if (modelTemplate != null) {
            val modelName = modelTemplate.nameArgument()

            // Adding import for the generated model.
            imports.add(type.declaration.packageName.asString() + "." + modelName)

            // Appending to builder.
            append(modelName)
            appendNullability(type.nullability)
        } else {

            // Adding an import of the constructor parameter type.
            addImport(type.declaration.qualifiedName!!.asString())

            // Appending type and arguments.
            append(type.declaration.simpleName.asString())
            typeReference.element?.typeArguments?.let(::appendTypeArguments)

            // Appending nullability.
            appendNullability(type.nullability)
        }
    }

    private fun appendTypeArguments(typeArguments: List<KSTypeArgument>) {
        if (typeArguments.isEmpty()) return

        append("<")
        typeArguments.forEachIndexed { index, arg ->
            appendTypeArgument(arg)

            if (index < typeArguments.lastIndex) {
                append(", ")
            }
        }
        append(">")
    }

    private fun appendTypeArgument(typeArgument: KSTypeArgument) {
        when (val variance = typeArgument.variance) {
            Variance.STAR -> {
                append("*")
                return
            }
            Variance.COVARIANT, Variance.CONTRAVARIANT -> append("${variance.label} ")
            Variance.INVARIANT -> {
            }
        }

        if (typeArgument.type == null) {
            logger.error("Invalid type argument", typeArgument)
            return
        } else {
            appendTypeReference(typeArgument.type!!)
        }
    }

    private fun appendNullability(nullability: Nullability) {
        if (nullability == Nullability.NULLABLE) {
            append("?")
        }
    }

    private fun resolveTypeReference(typeReference: KSTypeReference): KSType {
        return typeCache[typeReference] ?: typeReference.resolve().also {
            typeCache[typeReference] = it
        }
    }

    private fun isValueObject(typeReference: KSTypeReference): Boolean {
        return isValueObject(resolveTypeReference(typeReference))
    }

    private fun isValueObject(type: KSType): Boolean {
        return type.declaration is KSClassDeclaration && (type.declaration as KSClassDeclaration).superTypes.any {
            resolveTypeReference(it).declaration.qualifiedName?.asString() == QualifiedNames.valueObject
        }
    }

    private fun appendUnwrappedTypeReference(typeReference: KSTypeReference) {
        val type = resolveTypeReference(typeReference)
        if (type.declaration !is KSClassDeclaration) {
            appendTypeReference(typeReference)
            return
        }

        for (superReference in (type.declaration as KSClassDeclaration).superTypes) {
            val superType = resolveTypeReference(superReference)

            when (superType.declaration.qualifiedName?.asString()) {
                QualifiedNames.valueObject -> {
                    val typeArgument = superReference.element?.typeArguments?.firstOrNull()

                    if (typeArgument != null) {
                        append(resolveTypeReference(typeArgument.type!!).declaration.simpleName.asString())
                        appendNullability(type.nullability)
                        return
                    }
                }
                QualifiedNames.iterable,
                QualifiedNames.list,
                QualifiedNames.collection -> {
                    val typeArgument = typeReference.element?.typeArguments?.firstOrNull()
                    append(type.declaration.simpleName.asString() + "<")
                    appendUnwrappedTypeReference(typeArgument!!.type!!)
                    append(">")
                    appendNullability(type.nullability)
                    return
                }
            }
        }

        appendTypeReference(typeReference)
    }

    private fun addImport(import: String) {
        if (!import.startsWith("kotlin")) {
            imports.add(import)
        }
    }

    private fun String.vName(): String = 'v' + replaceFirstChar { it.uppercaseChar() }
}
