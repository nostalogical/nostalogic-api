package net.nostalogic.utils

import java.util.*

object CollUtils {

    fun <T: Enum<T>, V> enumMapOf(vararg pair: Pair<T, V>): EnumMap<T, V> {
        val map = mapOf(*pair)
        return EnumMap(map)
    }

}
