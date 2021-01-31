package net.nostalogic.controllers

import net.nostalogic.config.Config
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.StatusCheck
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.persistence.repositories.ConfigRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/system")
class SystemController(@Autowired private val config: Config,
                       @Autowired private val configRepo: ConfigRepository) {

    @RequestMapping(path = ["/up"], method = [RequestMethod.GET], produces = ["application/json"])
    fun isUp(): String {
        return "Working"
    }

    @RequestMapping(path = ["/status"], method = [RequestMethod.GET], produces = ["application/json"])
    fun version(): StatusCheck {
        return StatusCheck(configRepo)
    }

    @RequestMapping(path = ["/settings"], method = [RequestMethod.GET], produces = ["application/json"])
    fun getSettings(): HashMap<Setting.Source, HashMap<String, String>> {
        if (!AccessQuery().simpleCheck(entity = NoEntity.SETTING, action = PolicyAction.READ))
            throw NoAccessException(101006, "Missing rights to view settings")
        return Config.getAllSettings()
    }

    @RequestMapping(path = ["/reload"], method = [RequestMethod.GET], produces = ["application/json"])
    fun reloadSettings(): HashMap<Setting.Source, HashMap<String, String>> {
        if (!AccessQuery().simpleCheck(entity = NoEntity.SETTING, action = PolicyAction.READ))
            throw NoAccessException(101007, "Missing rights to view or reload settings")
        config.reloadSettings()
        return Config.getAllSettings()
    }

    @RequestMapping(path = ["/update"], method = [RequestMethod.PUT], produces = ["application/json"])
    fun updateSetting(@RequestBody keyVal: Pair<String, String>, @RequestParam reload: Boolean = false):
            HashMap<Setting.Source, HashMap<String, String>> {
        if (!AccessQuery().simpleCheck(entity = NoEntity.SETTING, action = PolicyAction.EDIT))
            throw NoAccessException(101008, "Missing rights to update settings")
        Config.addSetting(Setting(keyVal.first, keyVal.second, Setting.Source.DATABASE))
        if (reload)
            config.reloadSettings()
        return Config.getAllSettings()
    }

}
