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

    @Transaction
    @Query("SELECT * FROM project WHERE project.projectId = :projectId")
    fun getTimeEntriesForProject(projectId: Long): List<ProjectWithTimeEntries>

}