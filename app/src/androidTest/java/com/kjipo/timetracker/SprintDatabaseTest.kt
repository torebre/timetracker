package com.kjipo.timetracker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjipo.timetracker.database.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class SprintDatabaseTest {
    private lateinit var database: AppDatabase
    private lateinit var sprintDao: SprintDao
    private lateinit var context: Context

    @Before
    fun createDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        sprintDao = database.sprintDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun dayTypeForeignKeyTest() = kotlinx.coroutines.test.runTest {
        val startDate = LocalDate.of(2026, 5, 1)
        val endDate = LocalDate.of(2026, 5, 14)
        val sprint = Sprint(title = "Test Sprint", startDate = startDate, endDate = endDate)
        val sprintId = sprintDao.insertSprint(sprint)

        // Try to insert a SprintDay with a non-existent dayTypeId
        val invalidSprintDay = SprintDay(sprintId = sprintId, dayTypeId = 999L, date = startDate)
        
        var exceptionThrown = false
        try {
            sprintDao.insertSprintDay(invalidSprintDay)
        } catch (e: Exception) {
            // Room might wrap the exception or throw the native one
            if (e.toString().contains("FOREIGN KEY") || e is android.database.sqlite.SQLiteConstraintException) {
                exceptionThrown = true
            }
        }
        
        assert(exceptionThrown) { "Expected foreign key constraint exception for invalid dayTypeId" }
    }

    @Test
    fun sprintIdForeignKeyTest() = kotlinx.coroutines.test.runTest {
        val dayType = DayType(title = "Holiday", workingHours = 0.0)
        val dayTypeId = sprintDao.insertDayType(dayType)

        // Try to insert a SprintDay with a non-existent sprintId
        val invalidSprintDay = SprintDay(sprintId = 999L, dayTypeId = dayTypeId, date = LocalDate.now())
        
        var exceptionThrown = false
        try {
            sprintDao.insertSprintDay(invalidSprintDay)
        } catch (e: Exception) {
            if (e.toString().contains("FOREIGN KEY") || e is android.database.sqlite.SQLiteConstraintException) {
                exceptionThrown = true
            }
        }
        
        assert(exceptionThrown) { "Expected foreign key constraint exception for invalid sprintId" }
    }

    @Test
    fun sprintDayAndDayTypeTest() = kotlinx.coroutines.test.runTest {
        val sprint = Sprint(title = "Sprint", startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(7))
        val sprintId = sprintDao.insertSprint(sprint)
        
        val dayType = DayType(title = "Holiday", workingHours = 0.0)
        val dayTypeId = sprintDao.insertDayType(dayType)
        
        val date = LocalDate.now().plusDays(1)
        val sprintDay = SprintDay(sprintId = sprintId, dayTypeId = dayTypeId, date = date)
        sprintDao.insertSprintDay(sprintDay)
        
        val sprintDays = sprintDao.getSprintDays(sprintId)
        assertEquals(1, sprintDays.size)
        assertEquals(dayTypeId, sprintDays[0].dayTypeId)
        assertEquals(date, sprintDays[0].date)
    }

    @Test
    fun customDayTest() = kotlinx.coroutines.test.runTest {
        val sprint = Sprint(title = "Sprint", startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(7))
        val sprintId = sprintDao.insertSprint(sprint)
        
        val date = LocalDate.now().plusDays(2)
        val customDay = CustomDay(sprintId = sprintId, date = date, workingHours = 5.5)
        sprintDao.insertCustomDay(customDay)
        
        val customDays = sprintDao.getCustomDays(sprintId)
        assertEquals(1, customDays.size)
        assertEquals(5.5, customDays[0].workingHours, 0.01)
        assertEquals(date, customDays[0].date)
    }
}
