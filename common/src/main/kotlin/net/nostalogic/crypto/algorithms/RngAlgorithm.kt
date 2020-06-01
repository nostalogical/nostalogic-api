package net.nostalogic.crypto.algorithms

import java.security.SecureRandom

enum class RngAlgorithm(private val algorithmName: String) {

    SHA1PRNG("SHA1PRNG");

    fun getSecureRandom(): SecureRandom {
        return SecureRandom.getInstance(algorithmName)
    }
}
