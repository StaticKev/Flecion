package com.statickev.flecion.presentation.presentationUtil

import java.time.LocalDateTime

fun remindAtIsValid(remindAt: LocalDateTime, due: LocalDateTime?, doAt: LocalDateTime?): String? {
    return when {
        due != null && remindAt.isAfter(due) ->
            "This value cannot exceed the due date!" + due.toString()
        doAt != null && remindAt.isAfter(doAt) ->
            "This value cannot exceed the do at date!"
        remindAt.isBefore(LocalDateTime.now()) ->
            "Value must be later than now!"
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