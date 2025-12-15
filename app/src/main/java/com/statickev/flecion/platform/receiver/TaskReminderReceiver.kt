package com.statickev.flecion.platform.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statickev.flecion.platform.notification.NotificationHelper
import com.statickev.flecion.platform.scheduler.TASK_TITLE

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val taskTitle = intent?.getStringExtra(TASK_TITLE) ?: "Task reminder"

        context?.let {
            NotificationHelper.showNotification(
                context,
                taskTitle
            )
        }
    }
}