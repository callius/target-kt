package target.annotation_processor.core.domain

import com.squareup.kotlinpoet.MemberName

object MemberNames {

    val toNonEmptyListOrNull = MemberName("arrow.core", "toNonEmptyListOrNull")

    val validate = MemberName("target.core", "validate")
}
