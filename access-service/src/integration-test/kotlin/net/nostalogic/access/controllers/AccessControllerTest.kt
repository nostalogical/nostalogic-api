package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.config.DatabaseLoader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class AccessControllerTest(
        @Autowired val dbLoader: DatabaseLoader
) {

    private val localhost = "http://localhost:"

    @Value("\${local.server.port}")
    private var port: Int? = null
    private var baseApiUrl = localhost

//    private val userId = "TestUserId"
//    private val group1 = "group1"
//    private val group2 = "group2"
//    private val additional = setOf(group1, group2)

    @BeforeEach
    fun setup() {
        baseApiUrl = localhost + port
        dbLoader.runDbCleanSetup()
    }

    @AfterEach
    fun teardown() {
        dbLoader.runDataWipeScripts()
        dbLoader.runSchemaDropScripts()
    }

    private fun accessUrl(): String {
        return baseApiUrl + AccessController.ACCESS_ENDPOINT
    }

    @Test
    fun `Create a policy`() {

    }

}
