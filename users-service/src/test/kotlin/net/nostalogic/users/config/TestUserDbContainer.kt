package net.nostalogic.users.config

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class TestUserDbContainer :
    PostgreSQLContainer<TestUserDbContainer>(DockerImageName.parse(IMAGE_VERSION)) {

        companion object {
            private const val IMAGE_VERSION = "postgres:15-alpine"
            private var container: TestUserDbContainer? = null
            private var schema: String? = "test_nostalogic_users"

            fun getInstance(): TestUserDbContainer {
                if (container == null)
                    container = TestUserDbContainer()
                return container as TestUserDbContainer
            }
        }

    override fun start() {
        super.start()
        val schemaPrefix = if (jdbcUrl.contains('?')) "&" else "?"
        val urlWithSchema = "${jdbcUrl}${schemaPrefix}currentSchema=$schema"
        System.setProperty("spring.datasource.url", urlWithSchema)
        System.setProperty("spring.datasource.username", username)
        System.setProperty("spring.datasource.password", password)
    }
}
