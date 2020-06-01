package net.nostalogic.crypto.encoders

import kotlin.experimental.and

object HexEncoder {

    private val HEX_ARRAY = "0123456789abcdef".toCharArray()

    @ExperimentalUnsignedTypes
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)

        for (i in bytes.indices) {
            val j = (bytes[i].toUByte() and 0xff.toUByte()).toInt()
            hexChars[i * 2] = HEX_ARRAY[j ushr 4]
            hexChars[i * 2 + 1] = HEX_ARRAY[(j and 0x0f)]
        }
        return String(hexChars)
    }

    fun hexToBytes(hex: String): ByteArray {
        val hexChars = hex.toCharArray()
        val bytes = ByteArray(hex.length / 2)

        for (i in bytes.indices) {
            bytes[i] = ((hexToBase10(hexChars[i * 2]) shl 4) + hexToBase10(hexChars[i * 2 + 1])).toByte()
        }

        return bytes
    }

    private fun hexToBase10(hex: Char): Int {
        return Character.digit(hex, 16)
    }

}
