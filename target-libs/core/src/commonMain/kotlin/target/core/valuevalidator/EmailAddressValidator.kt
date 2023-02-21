package target.core.valuevalidator

import target.core.ValueObject

/**
 * A W3C HTML5 email address validator.
 */
abstract class EmailAddressValidator<T : ValueObject<String>>(ctor: (String) -> T) : StringInRegexValidator<T>(ctor) {

    override val regex by lazy {
        Regex(
            """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
        )
    }
}
