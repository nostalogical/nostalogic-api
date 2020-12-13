package net.nostalogic.users.config

import io.mockk.every
import io.mockk.mockk
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.persistence.entities.ConfigEntity
import net.nostalogic.persistence.repositories.ConfigRepository
import net.nostalogic.users.persistence.repositories.GroupRepository
import net.nostalogic.users.persistence.repositories.MembershipRepository
import net.nostalogic.users.persistence.repositories.UserRepository
import net.nostalogic.users.services.MembershipService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

open class UserTestConfig {

    @Autowired
    private lateinit var environment: Environment
    @Autowired
    private lateinit var context: ApplicationContext
    private val configRepo = mockk<ConfigRepository>()
    private val databaseLoader = mockk<DatabaseLoader>()
    private val membershipRepo = mockk<MembershipRepository>()
    private val userRepo = mockk<UserRepository>()
    private val groupRepo = mockk<GroupRepository>()

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
    open fun membershipRepository(): MembershipRepository {
        return membershipRepo
    }

    @Bean
    open fun membershipService(): MembershipService {
        return MembershipService(membershipRepo, userRepo, groupRepo)
    }

}
