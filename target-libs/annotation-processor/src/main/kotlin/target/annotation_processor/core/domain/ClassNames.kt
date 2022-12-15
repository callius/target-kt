package target.annotation_processor.core.domain

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

object ClassNames {
    val either = ClassName("arrow.core", "Either")
    val left = ClassName("arrow.core", "Either", "Left")
    val right = ClassName("arrow.core", "Either", "Right")

    val option = ClassName("arrow.core", "Option")
    val none = ClassName("arrow.core", "None")
    val some = ClassName("arrow.core", "Some")

    val nel = ClassName("arrow.core", "Nel")
    val nonEmptyList = ClassName("arrow.core", "NonEmptyList")

    val buildable = ClassName("target.core", "Buildable")
}

fun eitherOf(left: TypeName, right: TypeName): TypeName = ClassNames.either.parameterizedBy(left, right)

fun optionOf(type: TypeName) = ClassNames.option.parameterizedBy(type)

fun nelOf(type: TypeName) = ClassNames.nel.parameterizedBy(type)

fun buildableOf(type: TypeName) = ClassNames.buildable.parameterizedBy(type)
