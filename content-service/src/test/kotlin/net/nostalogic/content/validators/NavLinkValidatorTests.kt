package net.nostalogic.content.validators

import net.nostalogic.content.datamodel.navigations.NavLink
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
class NavLinkValidatorTests {

    private fun navigation(): NavLink {
        return NavLink(id = null,
        parentId = null,
        text = "Text",
        icon = "Icon",
        path = "something")
    }

    @Test
    fun `Confirm a valid navigation on creation`() {
        NavValidator.validateNavigation(navigation(), create = true)
    }

    @Test
    fun `Confirm a valid navigation on edit`() {
        NavValidator.validateNavigation(navigation(), create = false)
    }

    @Test
    fun `Confirm a valid navigation on creation requires a parent ID with an extended paths`() {
        val nav = navigation()
        nav.path = "one/two"
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
        nav.parentId = EntityUtils.uuid()
        NavValidator.validateNavigation(nav, create = true)
    }

    @Test
    fun `Confirm a valid navigation on edit requires a parent ID with an extended paths`() {
        val nav = navigation()
        nav.path = "one/two"
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = false) }
        nav.parentId = EntityUtils.uuid()
        NavValidator.validateNavigation(nav, create = false)
    }

    @Test
    fun `Confirm a navigation path must be valid`() {
        val nav = navigation()
        nav.path = "invalid path_"
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = false) }
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
    }

    @Test
    fun `Confirm an empty or null path on create throws an error`() {
        val nav = navigation()
        nav.path = ""
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
        nav.path = null
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
    }

    @Test
    fun `Confirm an empty path on edit is ignored as an unchanged field`() {
        val nav = navigation()
        nav.path = ""
        NavValidator.validateNavigation(nav, create = false)
        nav.path = null
        NavValidator.validateNavigation(nav, create = false)
    }

    @Test
    fun `Confirm empty text on create throws an error`() {
        val nav = navigation()
        nav.text = ""
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
        nav.text = null
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
    }

    @Test
    fun `Confirm empty text on edit is ignored as an unchanged field`() {
        val nav = navigation()
        nav.path = ""
        NavValidator.validateNavigation(nav, create = false)
        nav.path = null
        NavValidator.validateNavigation(nav, create = false)
    }

    @Test
    fun `Confirm empty icon on create throws an error`() {
        val nav = navigation()
        nav.icon = ""
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
        nav.icon = null
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
    }

    @Test
    fun `Confirm empty icon on edit is ignored as an unchanged field`() {
        val nav = navigation()
        nav.icon = ""
        NavValidator.validateNavigation(nav, create = false)
        nav.icon = null
        NavValidator.validateNavigation(nav, create = false)
    }

    @Test
    fun `Confirm text longer over the limit throws an error`() {
        val nav = navigation()
        nav.text = "Text over the limit of 20 chars"
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = false) }
    }

    @Test
    fun `Confirm icon longer over the limit throws an error`() {
        val nav = navigation()
        nav.icon = "Text over the limit of 20 chars"
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = false) }
    }

    @Test
    fun `Confirm path longer over the limit throws an error`() {
        val nav = navigation()
        nav.path = "some/path/which/is/just/over/fifty/character/limit/now"
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = true) }
        assertThrows<NoValidationException> { NavValidator.validateNavigation(nav, create = false) }
    }

}
