package net.nostalogic.access.config

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class TestAccessDbContainer :
    PostgreSQLContainer<TestAccessDbContainer>(DockerImageName.parse(IMAGE_VERSION)) {

        companion object {
            private const val IMAGE_VERSION = "postgres:15-alpine"
            private var container: TestAccessDbContainer? = null
            private var schema: String = "test_nostalogic_access"

            fun getInstance(): TestAccessDbContainer {
                if (container == null)
                    container = TestAccessDbContainer()
                return container as TestAccessDbContainer
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
