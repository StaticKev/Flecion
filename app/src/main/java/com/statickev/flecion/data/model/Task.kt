package com.statickev.flecion.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "Title",
    val description: String?,
    val status: TaskStatus,
    val priorityLevel: Byte,
    val timeToCompleteMins: Int,
    val progressPercentage: Int = 0,
    val remindAt: LocalDateTime?,
    val due: LocalDateTime?,
    val doAt: LocalDateTime?,
    val addToCalendar: Boolean
) {
    @Ignore
    val actualTimeToCompleteMins: Int = timeToCompleteMins - (
            timeToCompleteMins * progressPercentage / 100
            )
    @Ignore
    val daysUntilDue: () -> Int = {
        if (due != null) {
            ChronoUnit.DAYS.between(
                LocalDate.now(),
                due
            ).toInt()
        } else 0
    }
    @Ignore
    val isOverdue: DueStatus = when {
        due?.isEqual(LocalDateTime.now()) == true -> DueStatus.DUE_TODAY
        due?.isBefore(LocalDateTime.now()) == true -> DueStatus.OVERDUE
        due?.isAfter(LocalDateTime.now()) == true -> DueStatus.UPCOMING
        else -> DueStatus.NO_DUE
    }
    // TODO: Determine how to recapitulate 'priorityScore'.
    @Ignore
    val priorityScore: Int = 0
}

enum class TaskStatus {
    ONGOING,
    ON_HOLD,
    PENDING
}

enum class DueStatus {
    UPCOMING,
    DUE_TODAY,
    OVERDUE,
    NO_DUE
}