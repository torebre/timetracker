package com.kjipo.timetracker.database

import androidx.room.*
import java.time.Instant
import java.time.Instant.now


@Dao
interface TaskDao {

    @Insert
    fun insertTask(task: Task): Long

    @Update
    fun updateTask(task: Task)

    @Delete
    fun deleteTask(task: Task)

    fun updateLastActiveForTask(task: Task) {
        updateTask(task.copy(lastUpdated = now()))
    }

    @Query("SELECT * FROM task")
    fun getTasks(): List<Task>

    @Transaction
    @Query("SELECT * FROM task WHERE task.taskId = :taskId")
    fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries?

    @Transaction
    @Query("SELECT * FROM task ORDER BY :sortColumn")
    fun getTasksWithTimeEntries(sortColumn: String): List<TaskWithTimeEntries>

    @Query("SELECT * FROM task WHERE task.taskId = :taskId")
    fun getTask(taskId: Long): Task?

    @Insert
    fun insertTaskAndTagCrossRef(tagTasksCrossRef: TagTasksCrossRef): Long

    @Delete
    fun removeTaskAndTagCrossRef(tagTasksCrossRef: TagTasksCrossRef)

}