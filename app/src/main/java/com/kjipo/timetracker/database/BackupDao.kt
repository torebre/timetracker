package com.kjipo.timetracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<Project>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntries(timeEntries: List<TimeEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntryTaskCrossRefs(refs: List<TimeEntryTaskCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagTaskCrossRefs(refs: List<TagTasksCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntryDays(days: List<TimeEntryDay>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprints(sprints: List<Sprint>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTypes(dayTypes: List<DayType>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprintDays(sprintDays: List<SprintDay>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomDays(customDays: List<CustomDay>)

    @Query("DELETE FROM TimeEntryTaskCrossRef")
    suspend fun deleteTimeEntryTaskCrossRefs()

    @Query("DELETE FROM TagTasksCrossRef")
    suspend fun deleteTagTaskCrossRefs()

    @Query("DELETE FROM TimeEntry")
    suspend fun deleteTimeEntries()

    @Query("DELETE FROM TimeEntryDay")
    suspend fun deleteTimeEntryDays()

    @Query("DELETE FROM SprintDay")
    suspend fun deleteSprintDays()

    @Query("DELETE FROM CustomDay")
    suspend fun deleteCustomDays()

    @Query("DELETE FROM Task")
    suspend fun deleteTasks()

    @Query("DELETE FROM Tag")
    suspend fun deleteTags()

    @Query("DELETE FROM Project")
    suspend fun deleteProjects()

    @Query("DELETE FROM Sprint")
    suspend fun deleteSprints()

    @Query("DELETE FROM DayType")
    suspend fun deleteDayTypes()
}
