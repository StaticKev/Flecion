package com.statickev.flecion.data.repository

import com.statickev.flecion.data.dao.TaskDAO
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.util.millisRangeFor
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskRepository (
    private val taskDao: TaskDAO
) {
    fun getTaskByStatus(status: TaskStatus): Flow<List<Task>> =
        taskDao.getTasksByStatus(status.name)

    fun getTaskById(id: Long): Flow<Task?> = taskDao.getTaskById(id)

    fun getTodaysTask(date: LocalDate): Flow<List<Task>> {
        val range = millisRangeFor(date)
        return taskDao.getTaskByDate(range.first, range.second)
    }

    fun getScheduledTasks() = taskDao.getScheduledTasks()

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