package eti.lucasgomes.makalu.components.format

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class MoneyFormatterTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    private val usFormatter get() = MoneyFormatter(Locale.US)

    @Test
    fun `formats us dollars with two decimals in the us locale`() {
        assertEquals("$1,234.50", usFormatter.format(BigDecimal("1234.5")))
    }

    @Test
    fun `pads integer amounts to two decimals`() {
        assertEquals("$5.00", usFormatter.format(BigDecimal("5")))
    }

    @Test
    fun `formats zero`() {
        assertEquals("$0.00", usFormatter.format(BigDecimal.ZERO))
    }

    @Test
    fun `rounds amounts with more than two decimals`() {
        assertEquals("$2.00", usFormatter.format(BigDecimal("1.999")))
        assertEquals("$1.23", usFormatter.format(BigDecimal("1.231")))
    }

    @Test
    fun `formats negative amounts`() {
        assertEquals("-$5.00", usFormatter.format(BigDecimal("-5")))
    }

    @Test
    fun `always uses us dollars regardless of locale currency`() {
        val formatted = MoneyFormatter(Locale.forLanguageTag("pt-BR")).format(BigDecimal("1234.5"))

        assertTrue(formatted.contains("US$"))
        assertTrue(formatted.contains("1.234,50"))
        assertFalse(formatted.contains("R$"))
    }

    @Test
    fun `uses the locale grouping and decimal separators`() {
        assertEquals("1.234,50 $", MoneyFormatter(Locale.GERMANY).format(BigDecimal("1234.5")))
    }
}
