package net.nostalogic.comms

import com.google.gson.reflect.TypeToken
import net.nostalogic.config.Config
import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.*
import net.nostalogic.entities.EntityStatus
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory

object AccessComms {

    private val logger = LoggerFactory.getLogger(AccessComms::class.java)
    private const val ACCESS_ENDPOINT = "/api/v0/access"
    private const val SESSIONS_ENDPOINT = "/api/v0/sessions"
    private const val POLICY_URN = "/policies"

    private fun headerWithToken(token: String): HashMap<String, String> {
        return hashMapOf(Pair("Content-Type", "application/json"), Pair(NoStrings.AUTH_HEADER, token))
    }

    fun query(accessQuery: AccessQuery): AccessReport {
        return try {
            val response = khttp.post(url = Config.accessUrl() + ACCESS_ENDPOINT, json = Serialiser.toJson(accessQuery), headers = Comms.HEADERS)
            Comms.parseResponse(response, AccessReport::class.java) ?: AccessReport()
        } catch (e: Exception) {
            logger.error("Request to access service failed", e)
            throw NoAccessException(101002, "Failed to connect to access service", Translator.translate("permissionMissing"))
        }
    }

    fun createPolicy(policy: Policy): Policy? {
        return try {
            val response = khttp.post(url = Config.accessUrl() + "$ACCESS_ENDPOINT$POLICY_URN", json = Serialiser.toJson(policy), headers = Comms.HEADERS)
            Comms.parseResponse(response, Policy::class.java, expectedCode = 201)
        } catch (e: Exception) {
            logger.error("Internal request to update a session failed with an exception", e)
            null
        }
    }

    fun retrievePolicies(subjects: Set<String> = emptySet(), resources: Set<String> = emptySet(),
                         status: Set<EntityStatus> = emptySet(), priority: Set<PolicyPriority> = emptySet(),
                         actions: Set<PolicyAction> = emptySet()): Collection<Policy>? {
        return try {
            val params = HashMap<String, String>()
            if (subjects.isNotEmpty()) params["subjects"] = subjects.joinToString(",")
            if (resources.isNotEmpty()) params["resources"] = resources.joinToString(",")
            if (status.isNotEmpty()) params["status"] = status.joinToString(",")
            if (priority.isNotEmpty()) params["priority"] = priority.joinToString(",")
            if (actions.isNotEmpty()) params["actions"] = actions.joinToString(",")
            val response = khttp.get(url = Config.accessUrl() + "$ACCESS_ENDPOINT$POLICY_URN", params = params, headers = Comms.HEADERS)
            return Comms.parsePagedResponse(response, type = object : TypeToken<NoPageResponse<Policy>>() {}.type)
        } catch (e: Exception) {
            logger.error("Internal request to update a session failed with an exception", e)
            null
        }
    }

    fun deletePolicy(policyId: String, hard: Boolean = false) {
        return try {
            val response = khttp.delete(url = Config.accessUrl() + "$ACCESS_ENDPOINT$POLICY_URN/$policyId?hard=$hard", headers = Comms.HEADERS)
            Comms.parseResponse(response, Unit::class.java)
            return
        } catch (e: Exception) {
            logger.error("Internal request to update a session failed with an exception", e)
        }
    }

    fun updatePolicy(policy: Policy): Policy? {
        return try {
            val response = khttp.put(url = Config.accessUrl() + "$ACCESS_ENDPOINT$POLICY_URN/${policy.id}", json = Serialiser.toJson(policy), headers = Comms.HEADERS)
            return Comms.parseResponse(response, Policy::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to update a session failed with an exception", e)
            null
        }
    }

    fun createSession(prompt: SessionPrompt): SessionSummary? {
        return try {
            val response = khttp.post(url = Config.accessUrl() + SESSIONS_ENDPOINT, json = Serialiser.toJson(prompt), headers = Comms.HEADERS)
            Comms.parseResponse(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to create a session failed with an exception", e)
            null
        }
    }

    fun refreshSession(token: String): SessionSummary? {
        return try {
            val response = khttp.put(url = Config.accessUrl() + SESSIONS_ENDPOINT, headers = headerWithToken(token))
            Comms.parseResponse(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to refresh a session failed with an exception", e)
            null
        }
    }

    fun endSession(token: String): SessionSummary? {
        return try {
            val response = khttp.delete(url = Config.accessUrl() + SESSIONS_ENDPOINT, headers = headerWithToken(token))
            Comms.parseResponse(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to end a session failed with an exception", e)
            null
        }
    }

    fun verifySession(token: String): SessionSummary? {
        return try {
            val response = khttp.get(url = Config.accessUrl() + SESSIONS_ENDPOINT, headers = headerWithToken(token))
            Comms.parseResponse(response, SessionSummary::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to verify a session failed with an exception", e)
            null
        }
    }

    fun updateSession(groups: HashSet<String>, userId: String) {
        try {
            val response = khttp.put(url = Config.accessUrl() + "$SESSIONS_ENDPOINT/update/$userId", data = Serialiser.serialise(groups), headers = Comms.HEADERS)
            Comms.parseResponse(response, Unit::class.java)
        } catch (e: Exception) {
            logger.error("Internal request to update a session failed with an exception", e)
        }
    }
}
