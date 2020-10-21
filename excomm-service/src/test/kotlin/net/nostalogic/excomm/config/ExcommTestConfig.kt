package net.nostalogic.excomm.config

import io.mockk.every
import io.mockk.mockk
import net.nostalogic.config.Config
import net.nostalogic.excomm.persistence.repositories.EmailRepository
import net.nostalogic.excomm.persistence.repositories.EmailTemplateRepository
import net.nostalogic.excomm.services.emails.EmailService
import net.nostalogic.excomm.services.emails.LocalEmailService
import net.nostalogic.persistence.entities.ConfigEntity
import net.nostalogic.persistence.repositories.ConfigRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

open class ExcommTestConfig {

    @Autowired private lateinit var environment: Environment
    @Autowired private lateinit var context: ApplicationContext
    private val configRepo = mockk<ConfigRepository>()

    init {
        every { configRepo.findAll() } returns ArrayList<ConfigEntity>()
    }

    @Bean
    open fun config(): Config {
        return Config(context, environment, configRepo)
    }

    @Bean
    open fun emailService(): EmailService {
        return LocalEmailService()
    }

    @Bean
    open fun emailRepository(): EmailRepository {
        return mockk<EmailRepository>()
    }

    @Bean
    open fun emailTemplateRepository(): EmailTemplateRepository {
        return mockk<EmailTemplateRepository>()
    }
}
