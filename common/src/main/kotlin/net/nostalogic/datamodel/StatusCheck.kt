package net.nostalogic.datamodel

import com.fasterxml.jackson.annotation.JsonInclude
import net.nostalogic.config.i18n.Translator
import net.nostalogic.persistence.entities.ConfigEntity
import net.nostalogic.persistence.repositories.ConfigRepository
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
class StatusCheck(configRepo: ConfigRepository) : Serializable {

    val currentTime = NoDate()
    var persistedTime: NoDate? = null
    var persistTimeStatus : String? = "Time persistence FAILED"
    var translation = "Translation FAILED"

    init {
        try {
            translation = Translator.translate("translationTest")
        } catch (e: Exception) {}
        try {
            val persistedDateEntity = configRepo.save(ConfigEntity("status_check", "date", NoDate()))
            persistedTime = NoDate(persistedDateEntity.lastSet)
            configRepo.delete(persistedDateEntity)
            persistTimeStatus = null
        } catch (e: Exception) {}
    }

}
