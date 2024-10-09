package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toClassName

fun KSType.toClassNameWithNullability() = toClassName().withNullability(nullability)
