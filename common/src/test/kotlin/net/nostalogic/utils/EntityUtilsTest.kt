package net.nostalogic.utils

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.utils.EntityUtils.fullEntityId
import net.nostalogic.utils.EntityUtils.isEntity
import net.nostalogic.utils.EntityUtils.isFullId
import net.nostalogic.utils.EntityUtils.isShortId
import net.nostalogic.utils.EntityUtils.isUuid
import net.nostalogic.utils.EntityUtils.toEntity
import net.nostalogic.utils.EntityUtils.toEntityRef
import net.nostalogic.utils.EntityUtils.toMaybeEntityRef
import net.nostalogic.utils.EntityUtils.uuid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(profiles = ["test"])
class EntityUtilsTest {

    @Test
    fun `Entity from lowercase name`() {
        val entity = toEntity("nav")
        Assertions.assertEquals(NoEntity.NAV, entity)
    }

    @Test
    fun `Entity from uppercase name`() {
        val entity = toEntity("ARTICLE")
        Assertions.assertEquals(NoEntity.ARTICLE, entity)
    }

    @Test
    fun `Entity from nonexistent name should throw an error`() {
        assertThrows<NoValidationException> { toEntity("something invalid") }
    }

    @Test
    fun `Full ID to entity reference`() {
        val uuid = uuid()
        val expectedRef = EntityReference(uuid, NoEntity.CONTAINER)
        val ref = toEntityRef(NoEntity.CONTAINER.name.toLowerCase() + "_" + uuid)
        Assertions.assertEquals(expectedRef, ref)
    }

    @Test
    fun `Full ID with capital entity to entity reference`() {
        val uuid = uuid()
        val expectedRef = EntityReference(uuid, NoEntity.CONTAINER)
        val ref = toEntityRef(NoEntity.CONTAINER.name + "_" + uuid)
        Assertions.assertEquals(expectedRef, ref)
        Assertions.assertEquals(expectedRef, toMaybeEntityRef(NoEntity.CONTAINER.name + "_" + uuid))
    }

    @Test
    fun `Entity to entity reference`() {
        val expectedRef = EntityReference(null, NoEntity.CONTAINER)
        val ref = toEntityRef(NoEntity.CONTAINER.name)
        Assertions.assertEquals(expectedRef, ref)
        Assertions.assertEquals(expectedRef, toMaybeEntityRef(NoEntity.CONTAINER.name))
    }

    @Test
    fun `Invalid entity to entity reference`() {
        assertThrows<NoValidationException> { toEntityRef("NotEntity") }
        Assertions.assertNull(toMaybeEntityRef("NotEntity"))
    }

    @Test
    fun `Invalid full ID to entity reference`() {
        assertThrows<NoValidationException> { toEntityRef("NotEntity_" + uuid()) }
        Assertions.assertNull(toMaybeEntityRef("NotEntity_" + uuid()))
    }

    @Test
    fun `Valid UUID`() {
        Assertions.assertTrue(isUuid(uuid()))
    }

    @Test
    fun `Invalid UUID`() {
        Assertions.assertFalse(isUuid("Not_A_UUID"))
    }

    @Test
    fun `Valid uppercase entity`() {
        Assertions.assertTrue(isEntity(NoEntity.ARTICLE.name))
    }

    @Test
    fun `Valid lowercase entity`() {
        Assertions.assertTrue(isEntity(NoEntity.SESSION.name.toLowerCase()))
    }

    @Test
    fun `Invalid entity`() {
        Assertions.assertFalse(isEntity("something"))
    }

    @Test
    fun `Specific short ID recognised`() {
        Assertions.assertTrue(isShortId(uuid()))
    }

    @Test
    fun `Entity short ID recognised`() {
        Assertions.assertTrue(isShortId(NoEntity.EMAIL.name))
    }

    @Test
    fun `Lowercase entity short ID recognised`() {
        Assertions.assertTrue(isShortId(NoEntity.EMAIL.name.toLowerCase()))
    }

    @Test
    fun `Full ID is not an short ID`() {
        Assertions.assertFalse(isShortId(fullEntityId(uuid(), NoEntity.SESSION)))
    }

    @Test
    fun `A UUID is not a full ID`() {
        Assertions.assertFalse(isFullId(uuid()))
    }

    @Test
    fun `An entity is a valid full ID`() {
        Assertions.assertTrue(isFullId(NoEntity.CONTAINER.name))
    }

    @Test
    fun `A full ID is recognised`() {
        Assertions.assertTrue(isFullId(NoEntity.ARTICLE.name.toLowerCase() + "_" + uuid()))
    }
}
