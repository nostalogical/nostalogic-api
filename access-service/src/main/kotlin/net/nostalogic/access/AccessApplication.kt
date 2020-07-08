package net.nostalogic.access

import net.nostalogic.config.ApiVersion
import net.nostalogic.config.Config
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["net.nostalogic"])
@EnableJpaRepositories(basePackages = ["net.nostalogic.access.persistence.repositories"])
open class AccessApplication {

    companion object {
        const val MAJOR = 0
        val API_VERSION = ApiVersion(MAJOR, 0, 1)
        val SERVICE = "access_service"

        init {
            Config.initService(SERVICE, API_VERSION)
        }
    }
}

/**
 * Error prefix 2_00_000*
 */
fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}
