package com.statickev.lumina.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(defaultValue = "Title")
    val title: String = "Title",
    val description: String? = null,
    @ColumnInfo(defaultValue = "PENDING")
    val status: TaskStatus,
    val priorityLevel: Int,
    val timeToCompleteMins: Int,
    val progressPercentage: Int = 0,
    val doAt: LocalDateTime? = null,
    val remindMinsBefore: Int = 0,
    val dueDate: LocalDate? = null,
    val preferredPeriod: FocusPeriod,
    val addToCalendar: Boolean
) {
    @Ignore
    val actualTimeToCompleteMins: Int = timeToCompleteMins - (
            timeToCompleteMins * progressPercentage / 100
            )
    // TODO: Count the number of days until the due date.
    @Ignore
    val daysUntilDue: Int = 0 // MODIFY_LATER
    @Ignore
    val isOverdue: DueStatus = when {
        dueDate?.isEqual(LocalDate.now()) == true -> DueStatus.DUE_TODAY
        dueDate?.isBefore(LocalDate.now()) == true -> DueStatus.OVERDUE
        dueDate?.isAfter(LocalDate.now()) == true -> DueStatus.UPCOMING
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

enum class FocusPeriod {
    MORNING,
    AFTERNOON,
    EVENING,
    ANYTIME
}

enum class DueStatus {
    UPCOMING,
    DUE_TODAY,
    OVERDUE,
    NO_DUE
}