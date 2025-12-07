package com.statickev.flecion.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun minsToFormattedDuration(mins: Int): String {
    val m: Int = mins % 60
    val h: Int = mins / 60

    return if (h == 0) String.format(Locale.getDefault(), "%dm", m)
    else if (m == 0) String.format(Locale.getDefault(), "%dh", h)
    else String.format(Locale.getDefault(), "%dh %dm", h, m)
}

fun getFormattedDateTime(dateTime: LocalDateTime): String {
    return getDateTimeFormatter().format(dateTime)
}

fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good Morning!"
        in 12..16 -> "Good Afternoon!"
        in 17..20 -> "Good Evening!"
        else -> "Good Night!"
    }
}

fun getConcatenatedDateTime(dateMillis: Long, timeMinutes: Int): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dateMillis

    calendar.set(Calendar.HOUR_OF_DAY, timeMinutes / 60)
    calendar.set(Calendar.MINUTE, timeMinutes % 60)

    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

    return formatter.format(calendar.time)
}