package com.statickev.lumina.data

import androidx.room.TypeConverter
import com.statickev.lumina.data.model.DueStatus
import com.statickev.lumina.data.model.FocusPeriod
import com.statickev.lumina.data.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = enumValueOf(value)

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? =
        value?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? =
        date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString() // e.g. "2025-11-05"
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromFocusPeriod(value: FocusPeriod): String = value.name

    @TypeConverter
    fun toFocusPeriod(value: String): FocusPeriod = enumValueOf(value)

    @TypeConverter
    fun fromDueStatus(value: DueStatus): String = value.name

    @TypeConverter
    fun toDueStatus(value: String): DueStatus = enumValueOf(value)
}