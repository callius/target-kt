package target.test.generatemodel

import arrow.core.Option
import target.annotation.Validatable

@Validatable
data class TestModelBuilder(
    val id: Option<PositiveInt>,
    val isEnabled: Option<Boolean>,
) {
    companion object
}
