package net.nostalogic.access.config

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class TestPostgresContainer :
    PostgreSQLContainer<TestPostgresContainer>(DockerImageName.parse(IMAGE_VERSION)) {

        companion object {
            private const val IMAGE_VERSION = "postgres:15-alpine"
            private var container: TestPostgresContainer? = null
            private var schema: String? = null

            fun getInstance(schemaName: String): TestPostgresContainer {
                schema = schemaName
                if (container == null)
                    container = TestPostgresContainer()
                return container as TestPostgresContainer
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
