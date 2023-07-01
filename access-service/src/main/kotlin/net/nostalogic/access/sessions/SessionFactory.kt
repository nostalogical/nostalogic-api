package net.nostalogic.access.sessions

import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.config.Config
import net.nostalogic.constants.AuthenticationSource
import net.nostalogic.constants.ExceptionCodes
import net.nostalogic.datamodel.NoDate
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.StringUtils
import java.time.temporal.ChronoUnit

abstract class SessionFactory(protected val sessionPrompt: SessionPrompt) {

    companion object {
        private const val sessionExpirationKey = "security.session.duration-minutes"

        fun create(sessionPrompt: SessionPrompt): SessionFactory {
            if (StringUtils.isBlank(sessionPrompt.userId))
                throw throw NoAuthException(ExceptionCodes._0201007, "Cannot create a session without a user ID")

            return when (sessionPrompt.type) {
                AuthenticationSource.USERNAME, AuthenticationSource.EMAIL -> LoginSessionFactory(sessionPrompt)
                AuthenticationSource.IMPERSONATION -> ImpersonationSessionFactory(sessionPrompt)
                AuthenticationSource.PASSWORD_RESET -> PasswordResetSessionFactory(sessionPrompt)
                else -> throw NoAuthException(ExceptionCodes._0201009, "Authentication type is not supported")
            }
        }

        fun standardSessionExpiration(from: NoDate = NoDate()): NoDate {
            return from.addTime(Config.getNumberSetting(sessionExpirationKey).toLong(), ChronoUnit.MINUTES)
        }
    }

    protected val sessionStart: NoDate = NoDate()
    protected lateinit var sessionEnd: NoDate
    protected val userId: String = sessionPrompt.userId
    protected val sessionId: String = EntityUtils.uuid()

    abstract fun createEntity(): ServerSessionEntity

    open fun endOtherSessions(): Boolean {
        return false
    }

}
