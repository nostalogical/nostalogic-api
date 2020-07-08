package net.nostalogic.exceptions

import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus

/**
 * Error code prefix: 7_000
 */
class NoValidationException(code: Int, fieldsList: String, debugMessage: String? = null, cause: Exception? = null)
    : NoException(code, cause, HttpStatus.BAD_REQUEST, debugMessage, ErrorStrings.fieldsInvalid(fieldsList))
