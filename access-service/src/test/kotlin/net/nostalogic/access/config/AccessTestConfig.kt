package net.nostalogic.access.config

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import net.nostalogic.access.persistence.repositories.ServerSessionEventRepository
import net.nostalogic.access.persistence.repositories.ServerSessionRepository
import net.nostalogic.access.services.SessionService
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.persistence.entities.ConfigEntity
import net.nostalogic.persistence.repositories.ConfigRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

open class AccessTestConfig {

    @Autowired private lateinit var environment: Environment
    @Autowired private lateinit var context: ApplicationContext
    private val configRepo = mockk<ConfigRepository>()

    private val sessionRepo = mockk<ServerSessionRepository>()
    private val sessionEventRepo = mockk<ServerSessionEventRepository>()
    private val databaseLoader = mockk<DatabaseLoader>()

    init {
        every { databaseLoader.runSchemaBuildScripts() } just runs
        every { configRepo.findAll() } returns ArrayList<ConfigEntity>()
    }

    @Bean
    open fun databaseLoader(): DatabaseLoader {
        return databaseLoader
    }

    @Bean
    open fun config(): Config {
        return Config(context, environment, configRepo)
    }

    @Bean
    open fun serverSessionRepository(): ServerSessionRepository {
        return sessionRepo
    }

    @Bean
    open fun serverSessionEventRepository(): ServerSessionEventRepository {
        return sessionEventRepo
    }

    @Bean
    open fun sessionService(): SessionService {
        return SessionService(sessionRepo, sessionEventRepo)
    }
}
