package target.annotation_processor.core.extension

import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

fun ClassName.withTypeArguments(arguments: List<TypeName>): TypeName {
    return if (arguments.isEmpty()) {
        this
    } else {
        this.parameterizedBy(arguments).copy(nullable = isNullable)
    }
}

fun ClassName.withNullability(nullability: Nullability): ClassName =
    copy(nullable = nullability == Nullability.NULLABLE) as ClassName

fun ClassName.withNullability(nullability: Boolean): ClassName = copy(nullable = nullability) as ClassName
