package target.annotation_processor.core.extension

import com.squareup.kotlinpoet.TypeName
import target.annotation_processor.core.domain.optionOf

/**
 * Wraps this in an 'Option' type.
 */
fun TypeName.asOption() = optionOf(this)

/**
 * Copies this with the given [nullable]. Only supports nullability as a boolean via [TypeName.isNullable].
 */
fun TypeName.withNullability(nullable: Boolean): TypeName = copy(nullable)
