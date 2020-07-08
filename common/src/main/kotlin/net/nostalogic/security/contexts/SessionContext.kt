package net.nostalogic.security.contexts

import net.nostalogic.config.Config
import net.nostalogic.security.grants.GuestGrant
import net.nostalogic.security.grants.NoGrant
import net.nostalogic.security.grants.TestGrant
import kotlin.concurrent.getOrSet

open class SessionContext (var token: String? = null, var grant: NoGrant = GuestGrant()) {

    companion object {
        val currentSession = ThreadLocal<SessionContext>()

        fun getUserId(): String {
            return getSession().grant.subject
        }

        fun getSession(): SessionContext {
            if (Config.isTest()) {
                currentSession.set(SessionContext(grant = TestGrant()))
            }
            return currentSession.getOrSet { SessionContext() }
        }
    }
}
