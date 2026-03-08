package com.kjipo.timetracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.Instant.now


@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    suspend fun updateLastActiveForTask(task: Task) {
        updateTask(task.copy(lastUpdated = now()))
    }

    @Query("SELECT * FROM task")
    suspend fun getTasks(): List<Task>

    @Transaction
    @Query("SELECT * FROM task WHERE task.taskId = :taskId")
    suspend fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries?

    @Transaction
    @Query("SELECT * FROM task ORDER BY :sortColumn")
    suspend fun getTasksWithTimeEntries(sortColumn: String): List<TaskWithTimeEntries>

    @Query("SELECT * FROM task WHERE task.taskId = :taskId")
    suspend fun getTask(taskId: Long): Task?

    @Transaction
    @Query("SELECT * FROM task")
    fun getAllTasksWithTimeEntriesFlow(): Flow<List<TaskWithTimeEntries>>

    @Insert
    suspend fun insertTaskAndTagCrossRef(tagTasksCrossRef: TagTasksCrossRef): Long

    @Delete
    suspend fun removeTaskAndTagCrossRef(tagTasksCrossRef: TagTasksCrossRef)

}