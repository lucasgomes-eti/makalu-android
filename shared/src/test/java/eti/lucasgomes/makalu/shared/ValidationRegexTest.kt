package eti.lucasgomes.makalu.shared

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationRegexTest {

    @Test
    fun `email regex accepts a simple address`() {
        assertTrue("user@example.com".matches(REGEX_EMAIL))
    }

    @Test
    fun `email regex accepts subdomains, digits, underscores and hyphens`() {
        assertTrue("user_name-1@mail.example.co".matches(REGEX_EMAIL))
    }

    @Test
    fun `email regex rejects an address without at sign`() {
        assertFalse("user.example.com".matches(REGEX_EMAIL))
    }

    @Test
    fun `email regex rejects an address without a top level domain`() {
        assertFalse("user@example".matches(REGEX_EMAIL))
    }

    @Test
    fun `email regex rejects an empty string`() {
        assertFalse("".matches(REGEX_EMAIL))
    }

    @Test
    fun `password regex accepts letters and digits`() {
        assertTrue("abc12345".matches(REGEX_PASSWORD))
    }

    @Test
    fun `password regex rejects letters only`() {
        assertFalse("abcdefgh".matches(REGEX_PASSWORD))
    }

    @Test
    fun `password regex rejects digits only`() {
        assertFalse("12345678".matches(REGEX_PASSWORD))
    }
}
