package com.statickev.flecion.data

import androidx.room.TypeConverter
import com.statickev.flecion.data.model.TaskStatus
import java.time.Instant
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
}