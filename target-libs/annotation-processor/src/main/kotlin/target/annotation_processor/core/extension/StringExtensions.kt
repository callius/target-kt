package target.annotation_processor.core.extension

fun String.appendParams(): String = "${this}Params"

fun String.appendBuilder(): String = "${this}Builder"

fun String.appendFieldFailure(): String = "${this}FieldFailure"

fun String.appendRequiredFieldFailure(): String = "${this}RequiredFieldFailure"
