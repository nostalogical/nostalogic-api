package net.nostalogic.content.config

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class TestContentDbContainer :
    PostgreSQLContainer<TestContentDbContainer>(DockerImageName.parse(IMAGE_VERSION)) {

        companion object {
            private const val IMAGE_VERSION = "postgres:15-alpine"
            private var container: TestContentDbContainer? = null
            private var schema: String? = "test_nostalogic_content"

            fun getInstance(): TestContentDbContainer {
                if (container == null)
                    container = TestContentDbContainer()
                return container as TestContentDbContainer
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
