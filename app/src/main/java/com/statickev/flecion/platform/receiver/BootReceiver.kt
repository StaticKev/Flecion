package com.statickev.flecion.platform.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statickev.flecion.data.AppDatabase
import com.statickev.flecion.data.dao.TaskDAO
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.platform.scheduler.scheduleTaskReminder
import com.statickev.flecion.util.toEpochMillis

// TODO: Scheduled notifications persist after reboot: STILL NOT TESTED!
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val dao: TaskDAO? = context?.let { AppDatabase.getInstance(context).taskDao() }
            dao?.let {
                val repo = TaskRepository(dao)
                val scheduledTasks = repo.getScheduledTasks()
                scheduledTasks.forEach { task ->
                    if (task.sendNotification) {
                        try {
                            task.remindAt?.let {
                                scheduleTaskReminder(
                                    context,
                                    task.id,
                                    task.title,
                                    it.toEpochMillis(),
                                    task.recurInterval != null
                                )
                            }
                        } catch (e: SecurityException) {}
                    }
                }
            }
        }
    }
}
