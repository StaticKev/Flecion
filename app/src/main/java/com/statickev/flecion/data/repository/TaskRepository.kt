package com.statickev.flecion.data.repository

import com.statickev.flecion.data.dao.TaskDAO
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.platform.scheduler.scheduleTaskReminder
import com.statickev.flecion.util.toEpochMillis
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class TaskRepository (
    private val taskDao: TaskDAO
) {
    fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()

    fun getOnHoldTasks(): Flow<List<Task>> = taskDao.getOnHoldTasks()

    fun getOngoingTasks(): Flow<List<Task>> = taskDao.getOngoingTasks()

    fun getTaskById(id: Int): Flow<Task?> = taskDao.getTaskById(id)

    fun getTaskByDoAt(doAt: LocalDateTime) = taskDao.getTaskByDate(doAt.toEpochMillis())

    // TODO: Delete on production.
    suspend fun insertTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks)
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}