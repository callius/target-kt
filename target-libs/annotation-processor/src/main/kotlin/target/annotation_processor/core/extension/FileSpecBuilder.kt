package target.annotation_processor.core.extension

import com.squareup.kotlinpoet.FileSpec

/**
 * Adds a comment denoting generated code.
 */
fun FileSpec.Builder.addGeneratedComment(): FileSpec.Builder {
    return addFileComment(
        "Generated code. Do not modify by hand."
    )
}
