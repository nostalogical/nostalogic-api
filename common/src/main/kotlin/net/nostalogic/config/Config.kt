package net.nostalogic.config

import net.nostalogic.datamodel.Setting
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.persistence.entities.ConfigEntity
import net.nostalogic.persistence.repositories.ConfigRepository
import net.nostalogic.security.utils.JwtUtil
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component(value = "Config")
@DependsOn(value = ["DatabaseLoader"])
class Config(private val context: ApplicationContext,
             private val environment: Environment,
             private var configRepository: ConfigRepository) {

    companion object {
        private val cache: HashMap<String, Setting> = HashMap()
        private const val ENVIRONMENT = "environment"
        private val censorList = setOf(
                JwtUtil.KEY_PROPERTY,
                "spring.datasource.security.password",
                "email.ses.secret-key"
        )
        private const val CENSORED = "*****"
        private const val SERVICE = "service"
        private const val API = "apiversion"
        private const val BASE_URL = "microservices.base-url"
        private const val ACCESS_PORT = "microservices.access-port"
        private const val EXCOMM_PORT = "microservices.excomm-port"
        private const val CLIENT_BASE_URL = "client.base-url"
        private const val CLIENT_PORT = "client.port"

        private var service = "unknown"
        private var apiVersion = ApiVersion(0, 0, 0)

        fun initService(service: String, apiVersion: ApiVersion) {
            this.service = service
            this.apiVersion = apiVersion
        }

        fun addSetting(setting: Setting) {
            cache[setting.key.toLowerCase()] = setting
            if (setting.key.toLowerCase() == JwtUtil.KEY_PROPERTY)
                JwtUtil.setKey(setting.value)
        }

        fun getEnvironment(): RunEnvironment {
            return RunEnvironment.valueOf(cache[ENVIRONMENT]?.value ?: RunEnvironment.TEST.name)
        }

        fun isUnitTest(): Boolean {
            return getEnvironment() == RunEnvironment.TEST
        }

        fun isTest(): Boolean {
            return getEnvironment() == RunEnvironment.TEST || getEnvironment() == RunEnvironment.INTEGRATION_TEST
        }

        fun isProd(): Boolean {
            return getEnvironment() == RunEnvironment.PRODUCTION
        }

        fun apiVersion(): String {
            return getSetting(API)
        }

        fun service(): String {
            return getSetting(SERVICE)
        }

        fun accessUrl(): String {
            return getSetting(BASE_URL) + getSetting(ACCESS_PORT)
        }

        fun excommUrl(): String {
            return getSetting(BASE_URL) + getSetting(EXCOMM_PORT)
        }

        fun frontendUrl(): String {
            return getSetting(CLIENT_BASE_URL) + getSetting(CLIENT_PORT)
        }

        fun getSetting(key: String): String {
            val setting = cache[key.toLowerCase()]?.value
            if (setting.isNullOrBlank())
                throw NoRetrieveException(103001, "Setting", "Setting $key not found in cache", null)
            return setting
        }

        fun getBoolean(key: String): Boolean {
            val setting = cache[key.toLowerCase()]?.value
            return !setting.isNullOrBlank() && setting.equals("true", true)
        }

        fun getNumberSetting(key: String): Int {
            return cache[key.toLowerCase()]?.numValue
                    ?: throw NoRetrieveException(103002, "Setting",
                            "Setting $key not found or not parsable as a number", null)
        }

        fun getAllSettings(): HashMap<Setting.Source, HashMap<String, String>> {
            val viewableCache = HashMap<Setting.Source, HashMap<String, String>>()
            for (source in Setting.Source.values())
                viewableCache[source] = HashMap()
            for (entry in cache) {
                if (censorList.contains(entry.key))
                    viewableCache[entry.value.source]?.put(entry.key, CENSORED)
                else
                    viewableCache[entry.value.source]?.put(entry.key, entry.value.value)
            }

            return viewableCache
        }
    }

    init {
        reloadSettings()
    }

    fun reloadSettings() {
        cache.clear()
        loadResourceSettings()
        loadDatabaseSettings()
        loadServiceSettings()
    }

    private fun loadServiceSettings() {
        addSetting(Setting(SERVICE, service, Setting.Source.SERVICE))
        addSetting(Setting(API, apiVersion.toString(), Setting.Source.SERVICE))
    }

    private fun loadDatabaseSettings() {
        val findAll : ArrayList<ConfigEntity> = configRepository.findAll() as ArrayList<ConfigEntity>
        for (entry in findAll) {
            addSetting(Setting(entry.name, entry.setting, Setting.Source.DATABASE))
        }
    }

    private fun loadResourceSettings() {
        val loader = YamlPropertySourceLoader()
        for (runEnv in listOf(
                RunEnvironment.DEFAULT,
                RunEnvironment.LOCAL,
                RunEnvironment.TEST,
                RunEnvironment.INTEGRATION_TEST,
                RunEnvironment.PRODUCTION)) {
            val resource = this.context.getResource("classpath:${runEnv.getYamlFile()}")
            val isActive = runEnv == RunEnvironment.DEFAULT || this.environment.activeProfiles.contains(runEnv.profile)
            if (isActive)
                addSetting(Setting(ENVIRONMENT, runEnv, Setting.Source.RESOURCE))
            if (resource.isFile && isActive) {
                val load = loader.load(null, resource)[0]
                val source = load.source
                if (source is Map<*,*>) {
                    for (key in source.keys) {
                        val value = load.getProperty(key as String)
                        if (value != null)
                            addSetting(Setting(key, value, Setting.Source.RESOURCE))
                    }
                }
            }
        }
    }

    enum class RunEnvironment(val profile: String) {
        DEFAULT("main"),
        PRODUCTION("production"),
        LOCAL("local"),
        TEST("test"),
        INTEGRATION_TEST("integration-test");

        fun getYamlFile(): String {
            return "application" + (if (this == DEFAULT) "" else "-$profile") + ".yaml"
        }
    }



}
