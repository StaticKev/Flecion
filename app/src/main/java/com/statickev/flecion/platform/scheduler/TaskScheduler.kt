package com.statickev.flecion.platform.scheduler

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.statickev.flecion.platform.receiver.TaskReminderReceiver

const val TASK_TITLE = "task_title"

@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun scheduleTaskReminder(
    context: Context,
    taskId: Long,
    taskTitle: String,
    triggerAtMillis: Long
) {
    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, TaskReminderReceiver::class.java).apply {
        putExtra(TASK_TITLE, taskTitle)
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
    val intent = Intent(context, TaskReminderReceiver::class.java)

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

