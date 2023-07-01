package net.nostalogic.utils

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.utils.EntityUtils.entityReference
import net.nostalogic.utils.EntityUtils.isEntity
import net.nostalogic.utils.EntityUtils.isEntityReference
import net.nostalogic.utils.EntityUtils.isLocalReference
import net.nostalogic.utils.EntityUtils.isUuid
import net.nostalogic.utils.EntityUtils.toEntity
import net.nostalogic.utils.EntityUtils.toEntityRef
import net.nostalogic.utils.EntityUtils.toMaybeEntityRef
import net.nostalogic.utils.EntityUtils.uuid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles(profiles = ["test"])
class EntityUtilsTest {

    @Test
    fun `An entity from a lowercase name is valid`() {
        val entity = toEntity(NoEntity.NAV.name.lowercase(Locale.getDefault()))
        Assertions.assertEquals(NoEntity.NAV, entity)
    }

    @Test
    fun `An entity from an uppercase name is valid`() {
        val entity = toEntity(NoEntity.ARTICLE.name.uppercase(Locale.getDefault()))
        Assertions.assertEquals(NoEntity.ARTICLE, entity)
    }

    @Test
    fun `An entity from a nonexistent name should throw an error`() {
        assertThrows<NoValidationException> { toEntity("something invalid") }
    }

    @Test
    fun `An entity reference is parsed as valid`() {
        val uuid = uuid()
        val expectedRef = EntityReference(uuid, NoEntity.EMAIL)
        val ref = toEntityRef(NoEntity.EMAIL.name.lowercase(Locale.getDefault()) + "_" + uuid)
        Assertions.assertEquals(expectedRef, ref)
    }

    @Test
    fun `An entity reference with a capitalised entity name is valid`() {
        val uuid = uuid()
        val expectedRef = EntityReference(uuid, NoEntity.EMAIL)
        val ref = toEntityRef(NoEntity.EMAIL.name + "_" + uuid)
        Assertions.assertEquals(expectedRef, ref)
        Assertions.assertEquals(expectedRef, toMaybeEntityRef(NoEntity.EMAIL.name + "_" + uuid))
    }

    @Test
    fun `An entity is a valid entity reference`() {
        val expectedRef = EntityReference(null, NoEntity.EMAIL)
        val ref = toEntityRef(NoEntity.EMAIL.name)
        Assertions.assertEquals(expectedRef, ref)
        Assertions.assertEquals(expectedRef, toMaybeEntityRef(NoEntity.EMAIL.name))
    }

    @Test
    fun `An invalid entity as an entity reference will throw an error`() {
        assertThrows<NoValidationException> { toEntityRef("NotEntity") }
        Assertions.assertNull(toMaybeEntityRef("NotEntity"))
    }

    @Test
    fun `An invalid entity reference will throw an error`() {
        assertThrows<NoValidationException> { toEntityRef("NotEntity_" + uuid()) }
        Assertions.assertNull(toMaybeEntityRef("NotEntity_" + uuid()))
    }

    @Test
    fun `A UUID is parsed as a valid UUID`() {
        Assertions.assertTrue(isUuid(uuid()))
    }

    @Test
    fun `A random string is no a valid UUID`() {
        Assertions.assertFalse(isUuid("Not_A_UUID"))
    }

    @Test
    fun `An entity name is a valid entity`() {
        Assertions.assertTrue(isEntity(NoEntity.ARTICLE.name))
    }

    @Test
    fun `A lowercase entity name is a valid entity`() {
        Assertions.assertTrue(isEntity(NoEntity.SESSION.name.lowercase(Locale.getDefault())))
    }

    @Test
    fun `A random word is not a valid entity`() {
        Assertions.assertFalse(isEntity("something"))
    }

    @Test
    fun `A UUID is a local reference`() {
        Assertions.assertTrue(isLocalReference(uuid()))
    }

    @Test
    fun `An entity name is a local reference`() {
        Assertions.assertTrue(isLocalReference(NoEntity.EMAIL.name))
    }

    @Test
    fun `A lowercase entity name is a local reference`() {
        Assertions.assertTrue(isLocalReference(NoEntity.EMAIL.name.lowercase(Locale.getDefault())))
    }

    @Test
    fun `An entity signature is not a local reference`() {
        Assertions.assertFalse(isLocalReference(entityReference(uuid(), NoEntity.SESSION).toString()))
    }

    @Test
    fun `A UUID is not an entity reference`() {
        Assertions.assertFalse(isEntityReference(uuid()))
    }

    @Test
    fun `An entity name is a valid entity reference`() {
        Assertions.assertTrue(isEntityReference(NoEntity.EMAIL.name))
    }

    @Test
    fun `An entity signature is recognised`() {
        Assertions.assertTrue(isEntityReference(NoEntity.ARTICLE.name.lowercase(Locale.getDefault()) + "_" + uuid()))
    }
}
