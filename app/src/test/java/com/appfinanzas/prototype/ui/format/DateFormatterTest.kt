package com.appfinanzas.prototype.ui.format

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatterTest {

    @Test
    fun `formats date as dd slash MM slash yyyy`() {
        assertEquals("21/09/2026", DateFormatter.format(LocalDate.of(2026, 9, 21)))
    }

    @Test
    fun `pads day and month with leading zeros`() {
        assertEquals("05/03/2025", DateFormatter.format(LocalDate.of(2025, 3, 5)))
    }
}