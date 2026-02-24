package com.statickev.flecion.presentation.presentationUtil

import com.statickev.flecion.data.model.TaskStatus
import java.time.LocalDateTime

fun remindAtIsValid(status: TaskStatus, remindAt: LocalDateTime?, due: LocalDateTime?, doAt: LocalDateTime?): String? {
    return when {
        due != null && remindAt?.isAfter(due) == true ->
            "This value cannot exceed the due date!$due"
        doAt != null && remindAt?.isAfter(doAt) == true ->
            "This value cannot exceed the do at date!"
        remindAt?.isBefore(LocalDateTime.now()) ?: false ->
            "Value must be later than now!"
        status == TaskStatus.ON_REPEAT && remindAt == null ->
            "Recurring task must have an initial date!"
        else -> null
    }
}

fun dueIsValid(due: LocalDateTime): String? {
    return when {
        due.isBefore(LocalDateTime.now()) ->
            "Value must be later than now!"
        else -> null
    }
}

fun doAtIsValid(due: LocalDateTime?, doAt: LocalDateTime): String? {
    return when {
        due != null && doAt.isAfter(due) ->
            "This value cannot exceed the due date!"
        doAt.isBefore(LocalDateTime.now()) ->
            "Value must be later than now!"
        else -> null
    }
}