package net.nostalogic.datamodel

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class ErrorResponse(
        var userMessage: String,
        var debugMessage: String?,
        var status: Int,
        var errorCode: Int)
