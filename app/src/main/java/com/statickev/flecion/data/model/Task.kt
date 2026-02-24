package com.statickev.flecion.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var title: String = "New Task",
    var description: String? = "",
    var status: TaskStatus = TaskStatus.PENDING,
    var priorityLevel: Byte? = 1,
    var timeToCompleteMins: Int = 0,
    var completionRate: Int? = 0,
    var remindAt: LocalDateTime? = null,
    var due: LocalDateTime? = null,
    var doAt: LocalDateTime? = null,
    var addToCalendar: Boolean = false,
    var googleEventId: String? = null,
    var sendNotification: Boolean = true,
    var recurInterval: Int? = null,
) {
    @get:Ignore
    val timeLeftToComplete: Int
        get() = completionRate?.let {
            timeToCompleteMins - (timeToCompleteMins * it / 100)
        } ?: 0

    @get:Ignore
    val priorityScore: Int
        get() {
            return priorityLevel?.let { it ->
                val daysToDue = due?.let {
                    java.time.Duration.between(LocalDateTime.now(), it)
                        .toDays().coerceAtLeast(0)
                } ?: 365

                val daysToDoAt = doAt?.let {
                    java.time.Duration.between(LocalDateTime.now(), it)
                        .toDays().coerceAtLeast(0)
                } ?: 365

                val normalizedPriorityLevel = it / 5.0
                val normalizedTimeLeft = timeLeftToComplete / 432059.0
                val normalizedDaysToDue = 1 - (daysToDue / 365.0)
                val normalizedDaysToDoAt = 1 - (daysToDoAt / 365.0)

                val rawScore = (normalizedPriorityLevel * 0.5) +
                        (normalizedTimeLeft * 0.2) +
                        (normalizedDaysToDue * 0.2) +
                        (normalizedDaysToDoAt * 0.1)

                (rawScore * 1000).toInt().coerceAtLeast(1)
            } ?: 0
        }
}

enum class TaskStatus {
    ONGOING,
    ON_HOLD,
    PENDING,
    ON_REPEAT
}