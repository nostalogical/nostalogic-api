package net.nostalogic.access

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["net.nostalogic"])
@EnableJpaRepositories(basePackages = ["net.nostalogic.access.persistence.repositories"])
open class AccessApplication {
}

/**
 * Error prefix 2_00_000*
 */
fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}
