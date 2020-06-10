package net.nostalogic.config

import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Component
import java.io.IOException
import java.sql.Connection


@Component(value = "DatabaseLoader")
class DatabaseLoader(
        private val resourceLoader: ResourceLoader,
        private val environment: Environment,
        jdbcTemplate: JdbcTemplate) {

    companion object {
        private const val BASE_SCHEMA_SQL_PATTERN = "_base_schema.sql"
        private const val SERVICE_SCHEMA_SQL_PATTERN = "_service_schema.sql"
        private const val STANDARD_SQL_PATTERN = "classpath*:sql/*/*.sql"
        private const val OVERRIDE_SQL_PATTERN = "classpath*:sql/???_*.sql"
        private const val DATA_SETUP_SQL_PATTERN = "classpath*:sql/02_data/*.sql"
        private const val DATA_DROP_SQL_PATTERN = "classpath*:sql/DataTeardown_*.sql"
        private const val SCHEMA_DROP_SQL_PATTERN = "classpath*:sql/SchemaTeardown_*.sql"
    }


    private val connection: Connection
    private val useDatabase: Boolean

    init {
        useDatabase = isDbActive(environment)
        var conn : Connection? = null
        val datasource = jdbcTemplate.dataSource
        if (datasource != null) {
            conn = datasource.connection
        }
        if (conn != null)
            connection = conn
        else
            throw RuntimeException("Failed to get connection object from JDBC template")

        runResourceScripts(getStartupScripts())
    }

    fun runDbCleanSetup() {
        runSchemaDropScripts()
        runResourceScripts(getStartupScripts())
    }

    fun runSchemaDropScripts() {
        runResourceScripts(getResourcesFromPattern(SCHEMA_DROP_SQL_PATTERN))
    }

    fun runDataWipeScripts() {
        runResourceScripts(getResourcesFromPattern(DATA_DROP_SQL_PATTERN))
    }

    fun runDataSetupScripts() {
        runResourceScripts(getResourcesFromPattern(DATA_SETUP_SQL_PATTERN))
    }

    private fun runResourceScripts(resources: Array<Resource>) {
        if (useDatabase) {
            for (script in resources)
                ScriptUtils.executeSqlScript(connection, script)
        }
    }

    private fun getStartupScripts(): Array<Resource> {
        val standardScripts = getResourcesFromPattern(STANDARD_SQL_PATTERN).toMutableList()
        val overrideScripts = getResourcesFromPattern(OVERRIDE_SQL_PATTERN)
        val scriptsToRun = ArrayList<Resource>()

        isolateSchema(standardScripts)

        standard@
        for (standard in standardScripts) {
            if (standard.url.file.contains("schema") && standardScripts.indexOf(standard) > 0)
                continue
            for (override in overrideScripts) {
                if (standard.url.file.substringAfterLast("/").startsWith(override.url.file.substringAfterLast("/").substring(0, 3))) {
                    scriptsToRun.add(override)
                    continue@standard
                }
            }
            scriptsToRun.add(standard)
        }

        return scriptsToRun.toArray(arrayOfNulls<Resource>(scriptsToRun.size))
    }

    private fun isolateSchema(scripts: MutableList<Resource>) {
        var baseSchema: Resource? = null
        var serviceSchema: Resource? = null
        for (script in scripts) {
            if (script.url.file.contains(BASE_SCHEMA_SQL_PATTERN))
                baseSchema = script
            if (script.url.file.contains(SERVICE_SCHEMA_SQL_PATTERN))
                serviceSchema = script
        }
        scripts.remove(baseSchema)
        scripts.remove(serviceSchema)

        if (serviceSchema != null)
            scripts.add(0, serviceSchema)
        else if (baseSchema != null)
            scripts.add(0, baseSchema)
        else
            throw RuntimeException("No suitable schema found for Database Loader, aborting")
    }

    private fun getResourcesFromPattern(pattern: String): Array<Resource> {
        return try {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern)
        } catch (e: IOException) {
            throw RuntimeException("Unable to load SQL files from resources")
        }
    }

    private fun isDbActive(environment: Environment): Boolean {
        val dbEnvironments = setOf(
                Config.RunEnvironment.LOCAL.profile,
                Config.RunEnvironment.PRODUCTION.profile,
                Config.RunEnvironment.INTEGRATION_TEST.profile)
        for (profile in environment.activeProfiles) {
            if (dbEnvironments.contains(profile))
                return true
        }
        return false
    }

}
