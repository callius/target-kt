package target.test.generatemodel

import target.annotation.Validatable

@Validatable
data class TestModel(
    val id: PositiveInt,
    val isEnabled: Boolean,
    val updated: Long,
    val created: Long,
) {
    companion object
}
