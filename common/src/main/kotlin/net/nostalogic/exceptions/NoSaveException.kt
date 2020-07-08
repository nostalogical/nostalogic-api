package net.nostalogic.exceptions

import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus

/**
 * Error code prefix: 5_000
 */
class NoSaveException(code: Int, objectName: String, debugMessage: String?,
                      cause: Exception? = null, status: HttpStatus? = null)
    : NoException(code, cause, status, debugMessage, ErrorStrings.cannotSave(objectName))
