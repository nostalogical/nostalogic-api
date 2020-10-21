package net.nostalogic.excomm

import net.nostalogic.config.ApiVersion
import net.nostalogic.config.Config
import net.nostalogic.excomm.services.emails.AWSEmailService
import net.nostalogic.excomm.services.emails.EmailService
import net.nostalogic.excomm.services.emails.LocalEmailService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["net.nostalogic", "net.nostalogic.config"])
@EnableJpaRepositories(basePackages = ["net.nostalogic.excomm.persistence.repositories"])
open class ExcommApplication {

    @Value("\${email.ses.enabled}")
    var awsEnabled: Boolean? = null
    @Value("\${email.ses.access-key}")
    var accessKey: String? = null
    @Value("\${email.ses.secret-key}")
    var secretKey: String? = null

    companion object {
        const val MAJOR = 0
        val API_VERSION = ApiVersion(MAJOR, 0, 1)
        val SERVICE = "excomm_service"

        init {
            Config.initService(SERVICE, API_VERSION)
        }
    }

    @Bean
    open fun emailService(): EmailService {
        return if (awsEnabled == true) AWSEmailService(accessKey!!, secretKey!!) else LocalEmailService()
    }

}

/**
 * Error prefix 4_00_000*
 */
fun main(args: Array<String>) {
    runApplication<ExcommApplication>(*args)
}
