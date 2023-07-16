package target.annotation_processor.core.domain

/**
 * A validation function as specified by a validation template interface.
 */
data class ValidationFunction(
    val name: String,
    val properties: List<ModelProperty>
)
