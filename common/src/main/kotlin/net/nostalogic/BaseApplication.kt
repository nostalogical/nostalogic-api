package net.nostalogic

import net.nostalogic.config.ApiVersion
import net.nostalogic.config.Config
import net.nostalogic.security.contexts.SessionContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.context.annotation.RequestScope

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["net.nostalogic.persistence.repositories"])
open class BaseApplication {

    companion object {
        const val MAJOR = 0
        val API_VERSION = ApiVersion(MAJOR, 0, 1)
        val SERVICE = "base_service"

        init {
            Config.initService(SERVICE, API_VERSION)
        }
    }

    @Bean
    @RequestScope
    open fun sessionContext(): SessionContext {
        return SessionContext()
    }
}

/**
 * Error prefix 1_00_000*
 */
fun main(args: Array<String>) {
    runApplication<BaseApplication>(*args)
}
