package net.nostalogic.access.config

import io.mockk.every
import io.mockk.mockk
import net.nostalogic.access.persistence.repositories.*
import net.nostalogic.access.services.AccessQueryService
import net.nostalogic.access.services.AccessService
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
    private val policyRepo = mockk<PolicyRepository>()
    private val actionRepo = mockk<PolicyActionRepository>()
    private val resourceRepo = mockk<PolicyResourceRepository>()
    private val subjectRepo = mockk<PolicySubjectRepository>()
    private val databaseLoader = mockk<DatabaseLoader>()
    private val accessQueryService = mockk<AccessQueryService>()

    init {
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
    open fun policyRepository(): PolicyRepository {
        return policyRepo
    }

    @Bean
    open fun policyActionRepository(): PolicyActionRepository {
        return actionRepo
    }

    @Bean
    open fun policyResourceRepository(): PolicyResourceRepository {
        return resourceRepo
    }

    @Bean
    open fun policySubjectRepository(): PolicySubjectRepository {
        return subjectRepo
    }

    @Bean
    open fun sessionService(): SessionService {
        return SessionService(sessionRepo, sessionEventRepo)
    }

    @Bean
    open fun accessQueryService(): AccessQueryService {
        return AccessQueryService(policyRepo, subjectRepo, resourceRepo, actionRepo)
    }

    @Bean
    open fun accessService(): AccessService {
        return AccessService(policyRepo, subjectRepo, resourceRepo, actionRepo, accessQueryService)
    }
}
