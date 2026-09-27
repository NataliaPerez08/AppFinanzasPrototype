package com.appfinanzas.prototype.ui.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateFormatter {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun format(date: LocalDate): String = date.format(formatter)
}