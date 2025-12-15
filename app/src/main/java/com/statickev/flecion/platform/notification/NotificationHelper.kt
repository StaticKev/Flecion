package com.statickev.flecion.platform.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.statickev.flecion.R

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

object NotificationHelper {
    fun showNotification(context: Context, message: String) {
        val notification = NotificationCompat.Builder(context, "task_reminder")
            .setSmallIcon(R.drawable.baseline_notifications_none_24) // TODO: Change this with app icon later!
            .setContentTitle("\uD83D\uDD14 You've got a task to do!")
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}