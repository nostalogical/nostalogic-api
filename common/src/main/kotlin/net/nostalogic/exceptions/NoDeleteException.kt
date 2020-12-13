package net.nostalogic.exceptions

import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus

/**
 * Error code prefix: 3_000
 */
class NoDeleteException(code: Int, objectName: String, debugMessage: String? = null, status: HttpStatus? = HttpStatus.NOT_FOUND, cause: Exception? = null)
    : NoException(code, cause, status, debugMessage, ErrorStrings.cannotDelete(objectName))
