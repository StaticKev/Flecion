package com.statickev.flecion.platform.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.statickev.flecion.R
import com.statickev.flecion.platform.scheduler.TASK_ID
import com.statickev.flecion.platform.worker.TaskReminderWorker

fun createNotificationChannel(context: Context) {
    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    val channel = NotificationChannel(
        "task_reminder",
        "Task Reminders",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Reminders for your tasks"
        enableVibration(true)
        setSound(soundUri, audioAttributes)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

object TaskScheduler {
    fun showNotification(context: Context, title: String) {
        val notification = NotificationCompat.Builder(context, "task_reminder")
            .setSmallIcon(R.drawable.baseline_notifications_none_24)
            .setContentTitle("🔔 You've got a task to do!")
            .setContentText(title)
            .setAutoCancel(true)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun enqueueReminderWorker(context: Context, taskId: Long) {
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(
                workDataOf(TASK_ID to taskId)
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "task_reminder_worker_$taskId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
}