package net.nostalogic.comms

import net.nostalogic.config.Config
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory

object AccessComms {

    private val logger = LoggerFactory.getLogger(AccessComms::class.java)
    private const val ACCESS_ENDPOINT = "/v0/access"

    fun query(accessQuery: AccessQuery): AccessReport {
        val response = khttp.post(url = Config.accessUrl() + ACCESS_ENDPOINT, json = Serialiser.toJson(accessQuery), headers = BaseComms.HEADERS)
        if (response.statusCode != 200) {
            logger.error("Access request returned non-OK response (${response.statusCode}): ${String(response.content)}")
            return AccessReport()
        }
        return Serialiser.fromJson(response.jsonObject, AccessReport::class.java) ?: AccessReport()
    }

}
