package com.kjipo.timetracker.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SprintDao {

    @Insert
    suspend fun insertSprint(sprint: Sprint): Long

    @Update
    suspend fun updateSprint(sprint: Sprint)

    @Delete
    suspend fun deleteSprint(sprint: Sprint)

    @Query("SELECT * FROM Sprint ORDER BY startDate DESC")
    fun getAllSprints(): Flow<List<Sprint>>

    @Query("SELECT * FROM Sprint WHERE sprintId = :sprintId")
    suspend fun getSprint(sprintId: Long): Sprint?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDayType(dayType: DayType): Long

    @Query("SELECT * FROM DayType")
    suspend fun getDayTypes(): List<DayType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprintDay(sprintDay: SprintDay)

    @Query("SELECT * FROM SprintDay WHERE sprintId = :sprintId")
    suspend fun getSprintDays(sprintId: Long): List<SprintDay>

    @Query("DELETE FROM SprintDay WHERE sprintId = :sprintId AND date = :date")
    suspend fun deleteSprintDay(sprintId: Long, date: LocalDate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomDay(customDay: CustomDay)

    @Query("SELECT * FROM CustomDay WHERE sprintId = :sprintId")
    suspend fun getCustomDays(sprintId: Long): List<CustomDay>

    @Query("DELETE FROM CustomDay WHERE sprintId = :sprintId AND date = :date")
    suspend fun deleteCustomDay(sprintId: Long, date: LocalDate)

    @Transaction
    suspend fun clearAndInsertSprintDays(sprintId: Long, sprintDays: List<SprintDay>) {
        deleteSprintDaysForSprint(sprintId)
        sprintDays.forEach { insertSprintDay(it) }
    }

    @Query("DELETE FROM SprintDay WHERE sprintId = :sprintId")
    suspend fun deleteSprintDaysForSprint(sprintId: Long)

    @Transaction
    suspend fun clearAndInsertCustomDays(sprintId: Long, customDays: List<CustomDay>) {
        deleteCustomDaysForSprint(sprintId)
        customDays.forEach { insertCustomDay(it) }
    }

    @Query("DELETE FROM CustomDay WHERE sprintId = :sprintId")
    suspend fun deleteCustomDaysForSprint(sprintId: Long)
}
