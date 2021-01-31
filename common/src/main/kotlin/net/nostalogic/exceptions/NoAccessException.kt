package net.nostalogic.exceptions

import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus

/**
 * Error code prefix: 1_000
 * This represents a denial of an authenticated user to access a resource.
 */
class NoAccessException(code: Int, debugMessage: String?, userMessage: String = Translator.translate(ErrorStrings.GENERIC_ACCESS),
                        cause: Exception? = null)
    : NoException(code, cause, HttpStatus.FORBIDDEN, debugMessage, userMessage)
