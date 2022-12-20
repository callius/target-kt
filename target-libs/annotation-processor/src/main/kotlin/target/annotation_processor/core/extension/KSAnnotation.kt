package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType

/**
 * Gets the 'name' argument from an annotation.
 */
fun KSAnnotation.nameArgument(): String = arguments.first { it.name!!.asString() == "name" }.value as String

/**
 * Gets the 'failure' argument from an annotation.
 */
fun KSAnnotation.idFieldArgument(): KSAnnotation =
    arguments.first { it.name?.asString() == "idField" }.value as KSAnnotation

/**
 * Gets the 'customId' argument from an annotation.
 */
fun KSAnnotation.customIdArgument(): Boolean = arguments.first { it.name?.asString() == "customId" }.value as Boolean

/**
 * Gets the 'type' argument from an annotation.
 */
fun KSAnnotation.typeArgument(): KSType = arguments.first { it.name?.asString() == "type" }.value as KSType

/**
 * Gets the 'ignore' argument from an annotation.
 */
fun KSAnnotation.ignoreArgument(): Boolean = arguments.first { it.name?.asString() == "ignore" }.value as Boolean

/**
 * Converts this to a list, then finds all 'AddField' annotations in it, recursively checking each annotation's annotations.
 */
fun Sequence<KSAnnotation>.findAddFields(): List<KSAnnotation> = toList().findAddFields()

/**
 * Finds all 'AddField' annotations in this, recursively checking each annotation's annotations.
 */
fun Iterable<KSAnnotation>.findAddFields(): List<KSAnnotation> = filterNot { it.isLangAnnotation() }.flatMap {
    if (it.shortName.asString() == "AddField") {
        listOf(it)
    } else {
        // NOTE: Resolving the annotation type could be delegated to a function which pulls from a cache of types.
        it.annotationType.resolve().declaration.annotations.findAddFields()
    }
}

/**
 * If this is a language/platform annotation.
 */
fun KSAnnotation.isLangAnnotation(): Boolean = isMetadata()
        || isRetention()
        || isTarget()

fun KSAnnotation.isMetadata(): Boolean = shortName.asString() == "Metadata"

fun KSAnnotation.isRetention(): Boolean = shortName.asString() == "Retention"

fun KSAnnotation.isTarget(): Boolean = shortName.asString() == "Target"
