package net.nostalogic.security.contexts

import net.nostalogic.comms.AccessComms
import net.nostalogic.config.Config
import net.nostalogic.constants.NoLocale
import net.nostalogic.constants.NoStrings
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.grants.*
import org.springframework.context.i18n.LocaleContextHolder
import java.util.*
import kotlin.concurrent.getOrSet

open class SessionContext (
        var token: String? = null,
        var grant: NoGrant = GuestGrant()
) {

    companion object {
        val currentSession = ThreadLocal<SessionContext>()

        fun getUserId(): String {
            return getSession().grant.subject
        }

        fun getGrant(): NoGrant {
            return getSession().grant
        }

        fun getToken(): String? {
            return getSession().token
        }

        fun isLoggedIn(): Boolean {
            return getToken() != null
        }

        private fun getSession(): SessionContext {
            if (Config.isTest() && currentSession.get()?.token == null) {
                currentSession.set(SessionContext(grant = TestGrant()))
            }
            return currentSession.getOrSet { SessionContext() }
        }

        fun getLocale(): NoLocale {
            return when (LocaleContextHolder.getLocale()) {
                Locale("no") -> NoLocale.no_NO
                else -> NoLocale.en_GB
            }
        }

        fun requireLogin() {
            if (Config.isTest())
                return
            val grant = getGrant()
            if (!(grant is LoginGrant || grant is ImpersonationGrant))
                throw NoAuthException(102003, "User login is required for this function", NoStrings.notLoggedIn())
            val session = getToken()?.let { AccessComms.verifySession(it) }
            if (session?.token == null)
                throw NoAuthException(102004, "User token is valid but the server session could not be verified", NoStrings.sessionVerifyFail())
        }
    }
}
