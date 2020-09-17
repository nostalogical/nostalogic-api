package net.nostalogic.exceptions

import net.nostalogic.datamodel.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

abstract class NoException(private val errorCode: Int, cause: Exception?,
                           status: HttpStatus?, debugMessage: String?,
                           private val userMessage: String) : Exception(debugMessage, cause) {

    val status: HttpStatus = status ?: HttpStatus.INTERNAL_SERVER_ERROR
    val debugMessage = debugMessage ?: cause?.message ?: userMessage

    fun toResponseEntity(): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(userMessage, debugMessage, status.value(), errorCode), status)
    }
}
