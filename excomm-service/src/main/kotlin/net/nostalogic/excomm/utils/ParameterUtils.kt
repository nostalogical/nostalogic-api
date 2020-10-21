package net.nostalogic.excomm.utils

import net.nostalogic.config.Config

object ParameterUtils {

    val PARAM_BASE_URL = "base_url"

    fun keyToParameter(key: String): String {
        return "{{${key.trim().toLowerCase()}}}"
    }

    private fun getGlobalParameters(): Map<String, String> {
        return hashMapOf(Pair(PARAM_BASE_URL, Config.frontendUrl()))
    }

    fun setParameters(rawValue: String, parameters: Map<String, String>): String {
        var processedValue = rawValue
        for (param in parameters)
            processedValue = processedValue.replace(keyToParameter(param.key), param.value, true)
        for (param in getGlobalParameters())
            processedValue = processedValue.replace(keyToParameter(param.key), param.value, true)
        return processedValue
    }
}
