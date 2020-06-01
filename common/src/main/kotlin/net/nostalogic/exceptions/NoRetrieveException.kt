package net.nostalogic.exceptions

import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus

/**
 * Error code prefix: 4_000
 */
class NoRetrieveException(code: Int, objectName: String, debugMessage: String?, cause: Exception?)
    : NoException(code, cause, HttpStatus.NOT_FOUND, debugMessage, ErrorStrings.notFound(objectName))
