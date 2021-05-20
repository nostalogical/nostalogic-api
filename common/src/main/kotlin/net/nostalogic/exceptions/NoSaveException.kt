package net.nostalogic.exceptions

import net.nostalogic.constants.ErrorStrings
import org.springframework.http.HttpStatus

/**
 * Error code prefix: 5_000
 */
class NoSaveException(code: Int, objectName: String, cause: Exception? = null,
                      debugMessage: String? = null, status: HttpStatus? = null)
    : NoException(code, cause, status, debugMessage ?: ErrorStrings.cannotSave(objectName), ErrorStrings.cannotSave(objectName))
