package net.nostalogic.content.validators

import net.nostalogic.content.datamodel.containers.Container
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
class ContainerValidatorTests {

    private fun container(): Container {
        return Container(type = NoEntity.ARTICLE.name,
            contentId = EntityUtils.uuid(),
            contentName = "Some name",
            navId = EntityUtils.uuid(),
            urn = "some/urn")
    }

    @Test
    fun `Confirm basic container validation`() {
        ContainerValidator.validate(container())
    }

    @Test
    fun `Confirm container type is restricted to the valid list`() {
        val container = container()
        container.type = NoEntity.EMAIL.name
        assertThrows<NoValidationException> { ContainerValidator.validate(container) }
    }

    @Test
    fun `Confirm container type is restricted to being a valid entity`() {
        val container = container()
        container.type = "Something invalid"
        assertThrows<NoValidationException> { ContainerValidator.validate(container) }
    }

    @Test
    fun `A null navigation ID is invalid`() {
        val container = container()
        container.navId = null
        assertThrows<NoValidationException> { ContainerValidator.validate(container) }
    }

    @Test
    fun `An empty navigation ID is invalid`() {
        val container = container()
        container.navId = " "
        assertThrows<NoValidationException> { ContainerValidator.validate(container) }
    }

    @Test
    fun `A null content ID is invalid`() {
        val container = container()
        container.contentId = null
        assertThrows<NoValidationException> { ContainerValidator.validate(container) }
    }

    @Test
    fun `An empty content ID is invalid`() {
        val container = container()
        container.contentId = " "
        assertThrows<NoValidationException> { ContainerValidator.validate(container) }
    }

}
