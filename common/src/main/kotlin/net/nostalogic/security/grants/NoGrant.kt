package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate
import java.io.Serializable

/**
 * A 'grant' is a grant of access to the holder for resources on the server. Simple implementations grant limited access,
 * e.g. are used as confirmation that a user owns an email address. More complicated implementations use the access
 * service to verify which resources the token holder is granted access to.
 */
abstract class NoGrant(
        val subject: String,
        val expiration: NoDate? = null,
        val type: AuthenticationType,
        val description: String? = null,
        val created: NoDate
) : Serializable
