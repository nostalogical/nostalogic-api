package net.nostalogic.datamodel

class ErrorResponse(
        var userMessage: String,
        var debugMessage: String,
        var status: Int,
        var errorCode: Int)
