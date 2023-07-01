package net.nostalogic.content.utils

import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils
import java.util.*

object PathUtils {

    private val VALID_URN = "^[\\da-zA-Z+_-]+$".toRegex()
    private val VALID_PATH = "^[/\\da-z+_-]+$".toRegex()

    /**
     * Remove any leading or trailing slashes, replace any multiple slashes with a single one, and convert to lowercase
     */
    fun sanitisePath(path: String): String {
        return RegExUtils.replacePattern(
            RegExUtils.replacePattern(path, "^/*|/*\$", "")
            ,
            "/+", "/").lowercase(Locale.getDefault())
    }

    fun isUrnValid(urn: String): Boolean {
        if (StringUtils.isNotBlank(urn))
            return VALID_URN.matches(urn)
        return false
    }

    fun isPathValid(path: String): Boolean {
        if (StringUtils.isNotBlank(path) && VALID_PATH.matches(path)) {
            val urns = path.split("/")
            if (urns.isEmpty())
                return false
            for (urn in urns) {
                if (!isUrnValid(urn))
                    return false
            }
            return true
        }
        return false
    }
}
