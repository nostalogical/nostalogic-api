package net.nostalogic.exceptions

import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus
import java.lang.Exception

/**
 * Error code prefix: 2_000
 * This error represents a failure to authenticate a user and returns a 401 error.
 */
class NoAuthException(code: Int, debugMessage: String?,
                      userMessage: String = Translator.translate(ErrorStrings.TOKEN_INVALID),
                      cause: Exception? = null)
    : NoException(code, cause, HttpStatus.UNAUTHORIZED, debugMessage, userMessage)
