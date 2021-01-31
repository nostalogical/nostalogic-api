package net.nostalogic.config

import net.nostalogic.datamodel.Setting
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.persistence.entities.ConfigEntity
import net.nostalogic.persistence.repositories.ConfigRepository
import net.nostalogic.security.utils.JwtUtil
import org.slf4j.LoggerFactory
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

        private val logger = LoggerFactory.getLogger(Config::class.java)
        private val cache: HashMap<String, Setting> = HashMap()
        private const val ENVIRONMENT = "environment"
        private val ENV_VAR_REGEX = "\\$\\{.+}".toRegex()
        private val censorList = setOf(
                JwtUtil.KEY_PROPERTY,
                "spring.datasource.security.password",
                "email.ses.secret-key"
        )
        private const val CENSORED = "*****"
        private const val SERVICE = "service"
        private const val API = "apiversion"
        private const val API_URL = "microservices.api-url"
        private const val API_ACCESS_URL = "microservices.api-access-url"
        private const val API_EXCOMM_URL = "microservices.api-excomm-url"
        private const val ACCESS_PORT = "microservices.access-port"
        private const val EXCOMM_PORT = "microservices.excomm-port"
        private const val CLIENT_BASE_URL = "client.base-url"
        private const val CLIENT_PORT = "client.port"

        private var service = "unknown"
        private var apiVersion = ApiVersion(0, 0, 0)

        fun initService(service: String, apiVersion: ApiVersion) {
            if (this.service == "unknown") {
                this.service = service
                this.apiVersion = apiVersion
            }
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
            return getSetting(API_ACCESS_URL) + getSetting(ACCESS_PORT, true)
        }

        fun excommUrl(): String {
            return getSetting(API_EXCOMM_URL) + getSetting(EXCOMM_PORT, true)
        }

        fun frontendUrl(): String {
            return getSetting(CLIENT_BASE_URL) + getSetting(CLIENT_PORT)
        }

        fun getSetting(key: String, allowEmpty: Boolean = false): String {
            val setting = cache[key.toLowerCase()]?.value
            if (setting.isNullOrBlank())
                if (allowEmpty) return ""
                else throw NoRetrieveException(103001, "Setting", "Setting $key not found in cache", null)
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
        if (environment.activeProfiles.isEmpty())
            logger.error("No active environment profile has been defined!")
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
            if (isActive) {
                addSetting(Setting(ENVIRONMENT, runEnv, Setting.Source.RESOURCE))
                if (resource.isReadable) {
                    val load = loader.load(null, resource)[0]
                    val source = load.source
                    if (source is Map<*,*>) {
                        for (key in source.keys) {
                            val value = load.getProperty(key as String)
                            if (value != null) {
                                val replaced = replaceEnvironmentalVariables(value)
                                if (replaced == value)
                                    addSetting(Setting(key, value, Setting.Source.RESOURCE))
                                else
                                    addSetting(Setting(key, replaced, Setting.Source.ENV_RESOURCE))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun replaceEnvironmentalVariables(input: Any): Any {
        if (input is String) {
            val envVars = ENV_VAR_REGEX.find(input)
            if (envVars != null) {
                val variable = envVars.groups[0]!!.value
                val key = variable.substring(2, variable.length - 1)
                val value = environment.getProperty(key)
                if (value != null)
                    return input.replace(variable, value)
            }
        }
        return input
    }

    enum class RunEnvironment(val profile: String) {
        DEFAULT("main"),
        PRODUCTION("prod"),
        LOCAL("local"),
        TEST("test"),
        INTEGRATION_TEST("integration-test");

        fun getYamlFile(): String {
            return "application" + (if (this == DEFAULT) "" else "-$profile") + ".yaml"
        }
    }



}
