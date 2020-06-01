package net.nostalogic.controllers

import net.nostalogic.config.Config
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.StatusCheck
import net.nostalogic.persistence.repositories.ConfigRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminController(private val config: Config,
                      private val configRepo: ConfigRepository) {

    @GetMapping("/status")
    fun version(): ResponseEntity<StatusCheck> {
        return ResponseEntity.ok(StatusCheck(configRepo))
    }

    @GetMapping("/settings")
    fun getSettings(): ResponseEntity<HashMap<Setting.Source, HashMap<String, String>>> {
        return ResponseEntity.ok(Config.getAllSettings())
    }

    @GetMapping("/reload")
    fun reloadSettings(): ResponseEntity<HashMap<Setting.Source, HashMap<String, String>>> {
        config.reloadSettings()
        return ResponseEntity.ok(Config.getAllSettings())
    }

}
