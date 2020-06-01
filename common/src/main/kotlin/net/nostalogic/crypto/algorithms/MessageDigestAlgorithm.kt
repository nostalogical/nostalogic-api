package net.nostalogic.crypto.algorithms

import java.security.MessageDigest

enum class MessageDigestAlgorithm(private val algorithmName: String) {

    SHA256("SHA-256"),
    SHA512("SHA-512");

    fun getDigest(): MessageDigest {
        return MessageDigest.getInstance(algorithmName)
    }
}
