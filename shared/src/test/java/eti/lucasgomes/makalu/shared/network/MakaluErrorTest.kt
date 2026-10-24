package eti.lucasgomes.makalu.shared.network

import eti.lucasgomes.makalu.testing.testJson
import org.junit.Assert.assertEquals
import org.junit.Test

class MakaluErrorTest {

    @Test
    fun `formatedMessage joins internal code and message`() {
        val error = MakaluError(httpCode = 400, message = "Invalid", internalCode = "MK-12")

        assertEquals("MK-12 - Invalid", error.formatedMessage)
    }

    @Test
    fun `decodes snake case fields including field errors`() {
        val json = """
            {
              "http_code": 422,
              "message": "Validation failed",
              "internal_code": "MK-7",
              "field_errors": [{"field": "email", "message": "Invalid email"}]
            }
        """.trimIndent()

        val error = testJson.decodeFromString<MakaluError>(json)

        assertEquals(
            MakaluError(
                httpCode = 422,
                message = "Validation failed",
                internalCode = "MK-7",
                fieldErrors = listOf(MakaluError.FieldError("email", "Invalid email"))
            ),
            error
        )
    }

    @Test
    fun `decodes missing field errors as an empty list`() {
        val json = """{"http_code": 404, "message": "Not found", "internal_code": "MK-3"}"""

        val error = testJson.decodeFromString<MakaluError>(json)

        assertEquals(emptyList<MakaluError.FieldError>(), error.fieldErrors)
    }
}
