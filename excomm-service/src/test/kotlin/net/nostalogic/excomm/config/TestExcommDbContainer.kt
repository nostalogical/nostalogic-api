package net.nostalogic.excomm.config

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class TestExcommDbContainer :
    PostgreSQLContainer<TestExcommDbContainer>(DockerImageName.parse(IMAGE_VERSION)) {

        companion object {
            private const val IMAGE_VERSION = "postgres:15-alpine"
            private var container: TestExcommDbContainer? = null
            private var schema: String? = "test_nostalogic_excomm"

            fun getInstance(): TestExcommDbContainer {
                if (container == null)
                    container = TestExcommDbContainer()
                return container as TestExcommDbContainer
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
