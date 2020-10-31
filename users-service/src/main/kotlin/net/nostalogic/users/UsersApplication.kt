package net.nostalogic.users

import net.nostalogic.config.ApiVersion
import net.nostalogic.config.Config
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["net.nostalogic", "net.nostalogic.config"])
@EnableJpaRepositories(basePackages = ["net.nostalogic.users.persistence.repositories"])
open class UsersApplication {

    companion object {
        const val MAJOR = 0
        val API_VERSION = ApiVersion(MAJOR, 0, 1)
        val SERVICE = "user_service"

        init {
            Config.initService(SERVICE, API_VERSION)
        }
    }
}

/**
 * Error prefix 3_00_000*
 */
fun main(args: Array<String>) {
    runApplication<UsersApplication>(*args)
}
