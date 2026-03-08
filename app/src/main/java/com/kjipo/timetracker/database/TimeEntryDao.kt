package com.kjipo.timetracker.database

import androidx.room.*

@Dao
interface TimeEntryDao {

    @Insert
    suspend fun insertTimeEntry(timeEntry: TimeEntry): Long

    @Update
    suspend fun updateTimeEntry(timeEntry: TimeEntry)

    @Delete
    suspend fun deleteTimeEntry(timeEntry: TimeEntry)

    @Query("SELECT * FROM timeEntry WHERE timeEntryId = :timeEntryId")
    suspend fun getTimeEntry(timeEntryId: Long): TimeEntry?

    @Query("SELECT * FROM timeEntry WHERE taskId = :taskId")
    suspend fun getTimeEntriesForTask(taskId: Long): List<TimeEntry>

    @Insert
    suspend fun insertTimeEntryDay(timeEntry: TimeEntryDay): Long

    @Update
    suspend fun updateTimeEntryDay(timeEntryDay: TimeEntryDay)

    @Delete
    suspend fun deleteTimeEntryDay(timeEntryDay: TimeEntryDay)

    @Query("SELECT * FROM timeEntryDay WHERE id = :timeEntryId")
    suspend fun getTimeEntryDay(timeEntryId: Long): TimeEntryDay?

    @Query("SELECT * FROM timeEntryDay WHERE taskId = :taskId")
    suspend fun getTimeEntriesDaysForTask(taskId: Long): List<TimeEntryDay>

}