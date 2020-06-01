package net.nostalogic.crypto.algorithms

import javax.crypto.SecretKeyFactory

enum class SecretKeyAlgorithm(private val algorithmName: String) {

    PBKDF2WithHmacSHA1("PBKDF2WithHmacSHA1");

    fun getSecretFactory(): SecretKeyFactory {
        return SecretKeyFactory.getInstance(algorithmName)
    }
}
