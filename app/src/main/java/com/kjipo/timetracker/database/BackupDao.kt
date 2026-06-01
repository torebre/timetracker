package com.kjipo.timetracker.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BackupDao {
    @Query("SELECT * FROM TimeEntry")
    suspend fun getTimeEntries(): List<TimeEntry>

    @Query("SELECT * FROM Task")
    suspend fun getTasks(): List<Task>

    @Query("SELECT * FROM Tag")
    suspend fun getTags(): List<Tag>

    @Query("SELECT * FROM TimeEntryTaskCrossRef")
    suspend fun getTimeEntryTaskCrossRefs(): List<TimeEntryTaskCrossRef>

    @Query("SELECT * FROM TagTasksCrossRef")
    suspend fun getTagTasksCrossRefs(): List<TagTasksCrossRef>

    @Query("SELECT * FROM TimeEntryDay")
    suspend fun getTimeEntryDays(): List<TimeEntryDay>

    @Query("SELECT * FROM Project")
    suspend fun getProjects(): List<Project>

    @Query("SELECT * FROM Sprint")
    suspend fun getSprints(): List<Sprint>

    @Query("SELECT * FROM DayType")
    suspend fun getDayTypes(): List<DayType>

    @Query("SELECT * FROM SprintDay")
    suspend fun getSprintDays(): List<SprintDay>

    @Query("SELECT * FROM CustomDay")
    suspend fun getCustomDays(): List<CustomDay>
}
