package com.statickev.flecion.platform.scheduler

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.statickev.flecion.platform.receiver.AppReceiver

const val TASK_ID = "task_id"
const val TASK_TITLE = "task_title"
const val TASK_IS_RECURRING = "is_recurring"

@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun scheduleTaskReminder(
    context: Context,
    taskId: Long,
    taskTitle: String,
    triggerAtMillis: Long,
    isRecurring: Boolean
) {
    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, AppReceiver::class.java).apply {
        putExtra(TASK_ID, taskId)
        putExtra(TASK_TITLE, taskTitle)
        putExtra(TASK_IS_RECURRING, isRecurring)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    } catch (e: SecurityException) {
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}

fun cancelTaskReminder(context: Context, taskId: Long) {
    val intent = Intent(context, AppReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (pendingIntent != null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

