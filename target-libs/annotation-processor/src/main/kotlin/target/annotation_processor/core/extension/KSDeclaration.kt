package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName

fun KSDeclaration.canonicalName(): String = "${packageName.asString()}.${simpleName.asString()}"

fun KSDeclaration.toClassNameWithoutParameters(): ClassName {
    val packageNameString = packageName.asString()
    return ClassName(
        packageNameString,
        qualifiedName!!.asString().removePrefix("$packageNameString.").split(".")
    )
}
