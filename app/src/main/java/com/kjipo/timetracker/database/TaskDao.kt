package com.kjipo.timetracker.database

import androidx.room.*


@Dao
interface TaskDao {

    @Insert
    fun insertTask(task: Task): Long

    @Update
    fun updateTask(task: Task)

    @Delete
    fun deleteTask(task: Task)

    @Query("SELECT * FROM task")
    fun getTasks(): List<Task>

    @Transaction
    @Query("SELECT * FROM task WHERE task.taskId = :taskId")
    fun getTimeEntriesForTask(taskId: Long): List<TaskWithTimeEntries>


}