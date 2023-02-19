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
    fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries?

    @Transaction
    @Query("SELECT * FROM task")
    fun getTasksWithTimeEntries(): List<TaskWithTimeEntries>

    @Query("SELECT * FROM task WHERE task.taskId = :taskId")
    fun getTask(taskId: Long): Task?

    @Insert
    fun insertTaskAndTagCrossRef(tagTasksCrossRef: TagTasksCrossRef): Long

    @Delete
    fun removeTaskAndTagCrossRef(tagTasksCrossRef: TagTasksCrossRef)

}