package net.nostalogic.security.contexts

import net.nostalogic.datamodel.NoDate
import net.nostalogic.security.grants.GuestGrant
import net.nostalogic.security.grants.NoGrant

open class SessionContext {

    companion object {
        val currentSession = ThreadLocal<SessionContext>()
    }

    val created = NoDate()
    var token: String? = null
    var grant: NoGrant = GuestGrant()
}
