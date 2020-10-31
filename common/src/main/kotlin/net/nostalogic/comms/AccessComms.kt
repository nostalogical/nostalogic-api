package net.nostalogic.comms

import net.nostalogic.config.Config
import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory

object AccessComms {

    private val logger = LoggerFactory.getLogger(AccessComms::class.java)
    private const val ACCESS_ENDPOINT = "/v0/access"
    private const val SESSIONS_ENDPOINT = "/v0/sessions"

    private fun headerWithToken(token: String): HashMap<String, String> {
        return hashMapOf(Pair("Content-Type", "application/json"), Pair(NoStrings.AUTH_HEADER, token))
    }

    fun query(accessQuery: AccessQuery): AccessReport {
        return try {
            val response = khttp.post(url = Config.accessUrl() + ACCESS_ENDPOINT, json = Serialiser.toJson(accessQuery), headers = Comms.HEADERS)
            Comms.exchange(response, AccessReport::class.java) ?: AccessReport()
        } catch (e: Exception) {
            logger.error("Request to access service failed", e)
            throw NoAccessException(101002, "Failed to connect to access service", Translator.translate("permissionMissing"))
        }
    }

    fun createSession(prompt: SessionPrompt): SessionSummary? {
        return try {
            val response = khttp.post(url = Config.accessUrl() + SESSIONS_ENDPOINT, json = Serialiser.toJson(prompt), headers = Comms.HEADERS)
            Comms.exchange(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to create a session failed with an exception", e)
            null
        }
    }

    fun refreshSession(token: String): SessionSummary? {
        return try {
            val response = khttp.put(url = Config.accessUrl() + SESSIONS_ENDPOINT, headers = headerWithToken(token))
            Comms.exchange(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to refresh a session failed with an exception", e)
            null
        }
    }

    fun endSession(token: String): SessionSummary? {
        return try {
            val response = khttp.delete(url = Config.accessUrl() + SESSIONS_ENDPOINT, headers = headerWithToken(token))
            Comms.exchange(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to end a session failed with an exception", e)
            null
        }
    }

    fun verifySession(token: String): SessionSummary? {
        return try {
            val response = khttp.get(url = Config.accessUrl() + SESSIONS_ENDPOINT, headers = headerWithToken(token))
            Comms.exchange(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to verify a session failed with an exception", e)
            null
        }
    }

    fun updateSession(groups: Set<String>, userId: String) {
        try {
            val response = khttp.put(url = Config.accessUrl() + "$SESSIONS_ENDPOINT/update/$userId", json = Serialiser.toJson(groups), headers = Comms.HEADERS)
            Comms.exchange(response, Unit::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to update a session failed with an exception", e)
        }
    }
}
