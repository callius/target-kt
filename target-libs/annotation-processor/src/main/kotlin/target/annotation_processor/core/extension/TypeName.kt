package target.annotation_processor.core.extension

import com.squareup.kotlinpoet.TypeName

/**
 * Copies this with the given [nullable]. Only supports nullability as a boolean via [TypeName.isNullable].
 */
fun TypeName.withNullability(nullable: Boolean): TypeName = copy(nullable)
