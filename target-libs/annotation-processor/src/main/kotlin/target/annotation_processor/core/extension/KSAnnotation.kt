package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.KSAnnotation

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
