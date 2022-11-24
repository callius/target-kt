package target.annotation_processor.core.visitor

import target.annotation_processor.core.domain.Indent

/**
 * Variation of [StringBuilderVisitorVoid] which internally manages indentation.
 */
abstract class IndentedStringBuilderVisitorVoid : StringBuilderVisitorVoid() {

    private var indent = ""

    protected fun incrementIndent() {
        indent += Indent.one
    }

    protected fun decrementIndent() {
        if (indent.length >= Indent.one.length) {
            indent = indent.substring(Indent.one.length)
        }
    }

    protected inline fun block(next: () -> Unit) {
        incrementIndent()
        next()
        decrementIndent()
    }

    protected fun appendIndent() {
        append(indent)
    }

    protected fun appendIndent(input: String) {
        appendIndent()
        append(input)
    }

    protected fun appendIndentLine(input: String) {
        appendIndent(input)
        appendLine()
    }

    protected inline fun createDataClass(className: String, properties: () -> Unit, content: () -> Unit) {
        appendLine("data class $className(")
        block(properties)
        appendLine(") {")
        block(content)
        appendLine("}")
    }

    protected inline fun appendCompanionObject(className: String? = null, content: () -> Unit) {
        if (className == null) {
            appendIndentLine("companion object {")
        } else {
            appendIndentLine("companion object $className {")
        }
        block(content)
        appendIndentLine("}")
    }
}
