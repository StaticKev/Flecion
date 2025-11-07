package com.statickev.lumina.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun getFormattedDate(localDate: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.getDefault())
    val formattedDate = localDate.format(formatter)

    return formattedDate
}