package target.annotation_processor.core.visitor

import com.google.devtools.ksp.symbol.KSVisitorVoid

/**
 * Visitor where generated content is incrementally appended to the [builder].
 */
abstract class StringBuilderVisitorVoid : KSVisitorVoid() {

    private val builder = StringBuilder()

    protected fun append(input: String) {
        builder.append(input)
    }

    protected fun appendLine(input: String) {
        builder.appendLine(input)
    }

    protected fun appendLine() {
        builder.appendLine()
    }

    protected fun build() = builder.toString()
}
