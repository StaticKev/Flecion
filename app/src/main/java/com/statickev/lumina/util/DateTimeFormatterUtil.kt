package com.statickev.lumina.util

import java.time.format.DateTimeFormatter
import java.util.Locale

fun getDayDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.getDefault())
}

fun getDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
}

fun getDateTimeFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
}