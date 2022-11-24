package target.annotation_processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import target.annotation_processor.core.domain.QualifiedNames

class ModelTemplateProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(QualifiedNames.modelTemplate)
            .filterIsInstance<KSClassDeclaration>()

        return if (!symbols.iterator().hasNext()) {
            emptyList()
        } else {
            symbols.forEach { it.accept(modelTemplateVisitorPoet(), Unit) }
            symbols.filterNot { it.validate() }.toList()
        }
    }

    private fun modelTemplateVisitorPoet() = ModelTemplateVisitorPoet(codeGenerator, logger)
}
