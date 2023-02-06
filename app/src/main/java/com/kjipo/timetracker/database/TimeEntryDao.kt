package com.kjipo.timetracker.database

import androidx.room.*

@Dao
interface TimeEntryDao {

    @Insert
    fun insertTimeEntry(timeEntry: TimeEntry): Long

    @Update
    fun updateTimeEntry(timeEntry: TimeEntry)

    @Delete
    fun deleteTimeEntry(timeEntry: TimeEntry)

    @Query("SELECT * FROM timeEntry WHERE timeEntryId = :timeEntryId")
    fun getTimeEntry(timeEntryId: Long): TimeEntry?

    @Query("SELECT * FROM timeEntry WHERE taskId = :taskId")
    fun getTimeEntriesForTask(taskId: Long): List<TimeEntry>



}