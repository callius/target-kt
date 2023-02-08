package target.core.valueobject

import target.core.ValueObject
import target.core.valuevalidator.StringInRegexValidator

/**
 * A W3C HTML5 email address.
 */
@JvmInline
value class EmailAddress private constructor(override val value: String) : ValueObject<String> {

    companion object : StringInRegexValidator<EmailAddress>(::EmailAddress) {

        override val regex by lazy {
            Regex(
                """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
            )
        }
    }
}
