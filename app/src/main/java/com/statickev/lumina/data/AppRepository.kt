package com.statickev.lumina.data

import com.statickev.lumina.data.dao.TaskDAO
import com.statickev.lumina.data.model.Task
import kotlinx.coroutines.flow.Flow

class AppRepository (
    private val taskDao: TaskDAO
) {

    // TODO: Modify 'getAllTasks()' function to return sorted data based on 'priorityScore'
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

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
