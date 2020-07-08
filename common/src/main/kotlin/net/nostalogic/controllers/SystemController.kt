package net.nostalogic.controllers

import net.nostalogic.config.Config
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.StatusCheck
import net.nostalogic.persistence.repositories.ConfigRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/system")
class SystemController(private val config: Config,
                       private val configRepo: ConfigRepository) {

    @RequestMapping(path = ["/status"], method = [RequestMethod.GET], produces = ["application/json"])
    fun version(): StatusCheck {
        return StatusCheck(configRepo)
    }

    @RequestMapping(path = ["/settings"], method = [RequestMethod.GET], produces = ["application/json"])
    fun getSettings(): HashMap<Setting.Source, HashMap<String, String>> {
        return Config.getAllSettings()
    }

    @RequestMapping(path = ["/reload"], method = [RequestMethod.GET], produces = ["application/json"])
    fun reloadSettings(): HashMap<Setting.Source, HashMap<String, String>> {
        config.reloadSettings()
        return Config.getAllSettings()
    }

    @RequestMapping(path = ["/update"], method = [RequestMethod.PUT], produces = ["application/json"])
    fun updateSetting(@RequestBody keyVal: Pair<String, String>, @RequestParam reload: Boolean = false):
            HashMap<Setting.Source, HashMap<String, String>> {
        Config.addSetting(Setting(keyVal.first, keyVal.second, Setting.Source.DATABASE))
        if (reload)
            config.reloadSettings()
        return Config.getAllSettings()
    }

}
