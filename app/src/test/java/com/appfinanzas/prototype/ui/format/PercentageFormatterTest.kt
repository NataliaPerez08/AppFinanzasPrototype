package com.appfinanzas.prototype.ui.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PercentageFormatterTest {

    @Test
    fun `formats positive percentage with sign`() {
        assertEquals("+15.9%", PercentageFormatter.format(15.89))
    }

    @Test
    fun `formats negative percentage with sign`() {
        assertEquals("-3.2%", PercentageFormatter.format(-3.2))
    }

    @Test
    fun `formats zero without sign`() {
        assertEquals("0.0%", PercentageFormatter.format(0.0))
    }

    @Test
    fun `formats positive without sign`() {
        assertEquals("42.0%", PercentageFormatter.format(42.0, includeSign = false))
    }

    @Test
    fun `detects positive`() {
        assertTrue(PercentageFormatter.isPositive(1.0))
        assertFalse(PercentageFormatter.isPositive(0.0))
    }

    @Test
    fun `detects negative`() {
        assertTrue(PercentageFormatter.isNegative(-1.0))
        assertFalse(PercentageFormatter.isNegative(1.0))
    }

    @Test
    fun `detects zero`() {
        assertTrue(PercentageFormatter.isZero(0.0))
        assertFalse(PercentageFormatter.isZero(0.1))
    }
}