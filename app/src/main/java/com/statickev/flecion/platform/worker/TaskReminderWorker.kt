package com.statickev.flecion.platform.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.statickev.flecion.data.AppDatabase
import com.statickev.flecion.data.dao.TaskDAO
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.platform.scheduler.TASK_ID
import com.statickev.flecion.platform.scheduler.scheduleTaskReminder
import com.statickev.flecion.util.toEpochMillis
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @SuppressLint("ScheduleExactAlarm")
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(TASK_ID, -1L)

        val dao: TaskDAO? = applicationContext.let { AppDatabase.getInstance(applicationContext).taskDao() }

        dao?.let {
            val repo = TaskRepository(dao)
            val task = repo.getTaskById(taskId).first() ?: return@let

            val remindAt = task.remindAt ?: return@let
            val recurInterval = task.recurInterval ?: return@let

            val nextRemindAtMillis = remindAt.toEpochMillis() +
                        TimeUnit.DAYS.toMillis(recurInterval.toLong())

            val nextRemindAt: LocalDateTime = Instant
                .ofEpochMilli(nextRemindAtMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            repo.updateTask(
                task.copy(
                    remindAt = nextRemindAt
                )
            )

            scheduleTaskReminder(
                applicationContext,
                task.id,
                task.title,
                nextRemindAtMillis,
                task.recurInterval != null
            )
        }

        return Result.success()
    }
}