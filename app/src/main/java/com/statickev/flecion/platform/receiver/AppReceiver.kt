package com.statickev.flecion.platform.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statickev.flecion.platform.notification.TaskScheduler
import com.statickev.flecion.platform.scheduler.TASK_ID
import com.statickev.flecion.platform.scheduler.TASK_TITLE

class AppReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val taskId = intent?.getLongExtra(TASK_ID, -1L) ?: return
        val taskTitle = intent?.getStringExtra(TASK_TITLE) ?: "TITLE_NOT_FOUND"

        TaskScheduler.showNotification(context, taskTitle)
        TaskScheduler.enqueueReminderWorker(context, taskId)
    }
}