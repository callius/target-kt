package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.TypeName
import target.annotation_processor.core.domain.optionOf

/**
 * Wraps this in an 'Option' type.
 */
fun TypeName.asOption() = optionOf(this)

/**
 * Copies this with the given [nullability]. Only supports nullability as a boolean via [TypeName.isNullable].
 */
fun TypeName.withNullability(nullability: Nullability): TypeName = copy(nullable = nullability == Nullability.NULLABLE)
