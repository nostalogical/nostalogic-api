package net.nostalogic.security.contexts

import net.nostalogic.constants.AuthenticationType
import org.apache.commons.lang3.StringUtils

class AuthenticationContext(val type: AuthenticationType) {

    var userId: String? = null
    var userName: String? = null
    var email: String? = null
    var password: String? = null
    var storedPassword: String? = null

    fun isVerifiable(): Boolean {
        if (type == AuthenticationType.LOGIN) {
            return hasRequiredFields(listOf(userId, password, storedPassword))
        }
        return false
    }

    private fun hasRequiredFields(fields: Collection<String?>): Boolean {
        for (field in fields)
            if (StringUtils.isBlank(field))
                return false
        return true
    }

}
