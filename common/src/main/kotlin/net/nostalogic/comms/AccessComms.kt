package net.nostalogic.comms

import net.nostalogic.config.Config
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory

object AccessComms {

    private val logger = LoggerFactory.getLogger(AccessComms::class.java)
    private val ACCESS_ENDPOINT = "/v0/access"

    fun query(accessQuery: AccessQuery): AccessReport {
        val serialisedRequest = Serialiser.toJson(accessQuery)
        if (serialisedRequest == null) {
            logger.error("Failed to serialise access request")
            return AccessReport()
        }
        val response = khttp.post(url = Config.accessUrl() + ACCESS_ENDPOINT, json = Serialiser.toJson(accessQuery), headers = BaseComms.HEADERS)
        return Serialiser.fromJson(response.jsonObject, AccessReport::class.java) ?: AccessReport()
    }

}
