package net.nostalogic.datamodel.authentication

data class EncodingDetails(
    val password: String,
    val salt: String? = null,
    val iterations: Int? = null
)
