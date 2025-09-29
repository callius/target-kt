package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.KSType

fun KSType.toClassNameWithoutParameters() = declaration.toClassNameWithoutParameters()
