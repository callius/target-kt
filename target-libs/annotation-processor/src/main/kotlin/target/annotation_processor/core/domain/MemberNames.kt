package target.annotation_processor.core.domain

import com.squareup.kotlinpoet.MemberName

object MemberNames {
    val flatMap = MemberName("arrow.core", "flatMap")
    val toNonEmptyListOrNull = MemberName("arrow.core", "toNonEmptyListOrNull")
}
