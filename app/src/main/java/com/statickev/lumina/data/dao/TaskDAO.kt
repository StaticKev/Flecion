package com.statickev.lumina.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.statickev.lumina.data.model.Task
import com.statickev.lumina.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

// TODO: Create a function to delete tasks if it's not within this week.
// TODO: Create a function to return the number of completed tasks this week.

@Dao
interface TaskDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE progressPercentage != 100")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING'")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'ON_HOLD'")
    fun getOnHoldTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'ONGOING'")
    fun getOngoingTasks(): Flow<List<Task>>
}