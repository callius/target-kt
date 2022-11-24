package target.annotation_processor.core.visitor

import com.google.devtools.ksp.symbol.*
import target.annotation_processor.core.domain.QualifiedNames

abstract class CachedTypeReferenceResolverVisitorVoid : KSVisitorVoid() {

    private val typeCache = mutableMapOf<KSTypeReference, KSType>()

    protected open fun resolveTypeReference(typeReference: KSTypeReference): KSType {
        return typeCache[typeReference] ?: typeReference.resolve().also {
            typeCache[typeReference] = it
        }
    }

    protected fun isValueObject(typeReference: KSTypeReference): Boolean {
        return isValueObject(resolveTypeReference(typeReference), typeReference.element?.typeArguments.orEmpty())
    }

    protected fun isValueObject(type: KSType, typeArguments: List<KSTypeArgument> = emptyList()): Boolean {
        return (type.declaration is KSClassDeclaration && (type.declaration as KSClassDeclaration).superTypes.any {
            resolveTypeReference(it).declaration.qualifiedName?.asString() == QualifiedNames.valueObject
        }) || typeArguments.any { it.type?.let(::isValueObject) ?: false }
    }
}
