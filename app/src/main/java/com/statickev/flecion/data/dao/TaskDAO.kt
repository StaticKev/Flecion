package com.statickev.flecion.data.dao

import android.R
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.statickev.flecion.data.model.Task
import kotlinx.coroutines.flow.Flow

// TODO: Create a function to return the number of completed tasks this week.

@Dao
interface TaskDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    // TODO: Delete on production.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

//    @Query("SELECT * FROM tasks WHERE status = 'PENDING'")
//    fun getPendingTasks(): Flow<List<Task>>
//
//    @Query("SELECT * FROM tasks WHERE status = 'ON_HOLD'")
//    fun getOnHoldTasks(): Flow<List<Task>>
//
//    @Query("SELECT * FROM tasks WHERE status = 'ONGOING'")
//    fun getOngoingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<Task?>

    @Query("""
        SELECT * FROM tasks 
        WHERE doAt BETWEEN :startOfDay AND :endOfDay
        ORDER BY doAt
    """)
    fun getTaskByDate(startOfDay: Long, endOfDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE doAt IS NOT NULL")
    fun getScheduledTasks(): List<Task>
}