package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.KSPropertyDeclaration

/**
 * If this declaration is annotated with 'External'.
 */
fun KSPropertyDeclaration.isExternal(): Boolean = annotations.any { it.shortName.asString() == "External" }
