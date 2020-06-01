package net.nostalogic.exceptions

import org.springframework.http.HttpStatus

/**
 * Error code prefix: 6_000
 */
class NoSecurityException(code: Int, userMessage: String, debugMessage: String?, status: HttpStatus?, cause: Exception?)
    : NoException(code, cause, status ?: HttpStatus.INTERNAL_SERVER_ERROR, debugMessage, userMessage)
