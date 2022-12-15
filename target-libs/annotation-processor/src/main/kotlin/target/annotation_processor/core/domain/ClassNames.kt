package target.annotation_processor.core.domain

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

object ClassNames {
    val either = ClassName("arrow.core", "Either")
    val right = ClassName("arrow.core", "Either", "Right")
    val option = ClassName("arrow.core", "Option")
    val some = ClassName("arrow.core", "Some")
    val none = ClassName("arrow.core", "None")

    val positiveInt = ClassName("target.core.valueobject", "PositiveInt")
    val buildable = ClassName("target.core", "Buildable")
    val int = ClassName("kotlin", "Int")
}

fun eitherOf(left: TypeName, right: TypeName): TypeName = ClassNames.either.parameterizedBy(left, right)

fun buildableOf(type: TypeName) = ClassNames.buildable.parameterizedBy(type)

fun optionOf(type: TypeName) = ClassNames.option.parameterizedBy(type)
