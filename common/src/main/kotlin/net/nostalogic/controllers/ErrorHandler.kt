package net.nostalogic.controllers

import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.exceptions.NoException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandler {

    @ExceptionHandler(NoException::class)
    fun handleGenericException(exception: NoException): ResponseEntity<ErrorResponse> {
        return exception.toResponseEntity()
    }
}
