package net.nostalogic.exceptions

import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus

/**
 * Error code prefix: 5_000
 */
class NoSaveException(code: Int, objectName: String, debugMessage: String?, status: HttpStatus?, cause: Exception?)
    : NoException(code, cause, status, debugMessage, ErrorStrings.cannotSave(objectName))
