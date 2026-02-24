package com.statickev.flecion.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDateTime.toEpochMillis(): Long {
    return this
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun getDayDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.getDefault())
}

fun getDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
}

fun getDateTimeFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
}

fun millisRangeFor(date: LocalDate): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    return start to end
}
