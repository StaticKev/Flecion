package com.statickev.flecion.data.repository

import com.statickev.flecion.data.dao.TaskDAO
import com.statickev.flecion.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository (
    private val taskDao: TaskDAO
) {
    fun getTaskById(id: Int): Flow<Task?> = taskDao.getTaskById(id)

    // TODO: Modify 'getAllTasks()' function to return sorted data based on 'priorityScore'
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()

    fun getOnHoldTasks(): Flow<List<Task>> = taskDao.getOnHoldTasks()

    fun getOngoingTasks(): Flow<List<Task>> = taskDao.getOngoingTasks()

    // TODO: Delete on production.
    suspend fun insertTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks)
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}