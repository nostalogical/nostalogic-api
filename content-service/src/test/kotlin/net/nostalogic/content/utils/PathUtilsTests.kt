package net.nostalogic.content.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
class PathUtilsTests {

    @Test
    fun `Confirm URN is valid`() {
        Assertions.assertTrue(PathUtils.isUrnValid("aValidUrn"))
    }

    @Test
    fun `Confirm numbers in a URN is valid`() {
        Assertions.assertTrue(PathUtils.isUrnValid("a1Valid2Urn3"))
    }

    @Test
    fun `Confirm spaces in a URN are invalid`() {
        Assertions.assertFalse(PathUtils.isUrnValid("an invalid Urn"))
    }

    @Test
    fun `Confirm underscores in a URN are valid`() {
        Assertions.assertTrue(PathUtils.isUrnValid("a_valid_urn"))
    }

    @Test
    fun `Confirm certain special characters are allowed`() {
        Assertions.assertTrue(PathUtils.isUrnValid("a_valid-urn+with_other+characters"))
    }

    @Test
    fun `Confirm other special characters are not allowed`() {
        Assertions.assertFalse(PathUtils.isUrnValid("an@invalid?urn+with(other)<characters>s"))
    }

    @Test
    fun `Confirm path is valid`() {
        Assertions.assertTrue(PathUtils.isPathValid("a/valid/path"))
    }

    @Test
    fun `Confirm path with upper case is invalid`() {
        Assertions.assertFalse(PathUtils.isPathValid("an/INvalid/path"))
    }

    @Test
    fun `Confirm path with spaces is invalid`() {
        Assertions.assertFalse(PathUtils.isPathValid("an/in valid/path"))
    }

    @Test
    fun `Confirm path with special characters is invalid`() {
        Assertions.assertFalse(PathUtils.isPathValid("an/in[val]id/pa=th"))
    }

    @Test
    fun `Confirm path with numbers and valid separators characters is valid`() {
        Assertions.assertTrue(PathUtils.isPathValid("a/path-with/numb3rs/and+symbols_too"))
    }

    @Test
    fun `Remove leading and trailing slashes`() {
        Assertions.assertEquals("leading/and/trailing/slashes", PathUtils.sanitisePath("/leading/and/trailing/slashes/"))
    }

    @Test
    fun `Remove multiple slashes`() {
        Assertions.assertEquals("remove/multiple/slashes", PathUtils.sanitisePath("//remove////multiple//slashes////"))
    }

}
