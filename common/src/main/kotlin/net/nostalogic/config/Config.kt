package net.nostalogic.config

import net.nostalogic.datamodel.Setting
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.persistence.entities.ConfigEntity
import net.nostalogic.persistence.repositories.ConfigRepository
import net.nostalogic.security.utils.JwtUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
@DependsOn(value = ["DatabaseLoader"])
class Config(private val context: ApplicationContext,
             private val environment: Environment,
             private var configRepository: ConfigRepository) {

    companion object {
        private val cache: HashMap<String, Setting> = HashMap()
        private const val ENVIRONMENT = "environment"
        private val censorList = setOf(
                JwtUtil.KEY_PROPERTY,
                "spring.datasource.security.password")
        private const val CENSORED = "*****"

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

        fun getSetting(key: String): String {
            val setting = cache[key.toLowerCase()]?.value
            if (setting.isNullOrBlank())
                throw NoRetrieveException(103001, "Setting", "Setting $key not found in cache", null)
            return setting
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

    private val springKeys: HashSet<String> = HashSet()

    init {
        springKeys.add("security.jwt.key")
        springKeys.add("security.jwt.duration-minutes")
        springKeys.add("spring.application.name")
        springKeys.add("spring.datasource.username")
        springKeys.add("spring.datasource.security.password")
        springKeys.add("spring.datasource.driverClassName")
        springKeys.add("spring.datasource.url")
        springKeys.add("spring.datasource.platform")
        springKeys.add("spring.jpa.show-sql")
        springKeys.add("spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults")
        springKeys.add("spring.jpa.open-in-view")
        springKeys.add("spring.jpa.database-platform")
        springKeys.add("spring.liquibase.default-schema")
        springKeys.add("server.port")
        reloadSettings()
    }

    fun reloadSettings() {
        cache.clear()
        loadResourceSettings(springKeys)
        loadDatabaseSettings()
    }

    private fun loadDatabaseSettings() {
        val findAll : ArrayList<ConfigEntity> = configRepository.findAll() as ArrayList<ConfigEntity>
        for (entry in findAll) {
            addSetting(Setting(entry.name, entry.setting, Setting.Source.DATABASE))
        }
    }

    private fun loadResourceSettings(keys: Collection<String>) {
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
                for (key in keys) {
                    val value = load.getProperty(key)
                    if (value != null)
                        addSetting(Setting(key, value, Setting.Source.RESOURCE))
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
