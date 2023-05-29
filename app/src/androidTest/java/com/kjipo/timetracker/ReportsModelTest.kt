package com.kjipo.timetracker

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.TagDao
import com.kjipo.timetracker.database.Task
import com.kjipo.timetracker.database.TaskDao
import com.kjipo.timetracker.database.TaskRepositoryImpl
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.database.TimeEntryDao
import com.kjipo.timetracker.database.TimeEntryDay
import com.kjipo.timetracker.reports.ReportsModel
import com.kjipo.timetracker.reports.SelectedTimeRange
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId


class ReportsModelTest {
    private lateinit var timeEntryDao: TimeEntryDao
    private lateinit var tagDao: TagDao
    private lateinit var taskDao: TaskDao
    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun createDatabase() {
        Timber.plant(Timber.DebugTree())

        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        timeEntryDao = database.timeEntryDao()
        tagDao = database.tagDao()
        taskDao = database.taskDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun timeEntryListContainsEntries() {
        val project =
            Project(0, "Project 1").also { addProject(it, database) }
        val project2 =
            Project(0, "Project 2").also { addProject(it, database) }

        val task = Task(0, "Task 1").also { addTask(it, database) }
            .also { setProjectForTask(project, it, database) }
        val task2 = Task(0, "Task 2").also { addTask(it, database) }
            .also { setProjectForTask(project2, it, database) }

        val taskRepository = TaskRepositoryImpl(database)

        val startTime = LocalDate.now().atTime(16, 0)
        val stopTime = LocalDate.now().atTime(18, 0)

        val timeEntry = TimeEntry(
            0, task.taskId, startTime.toInstant(ZoneId.systemDefault().rules.getOffset(startTime)),
            stopTime.toInstant(ZoneId.systemDefault().rules.getOffset(startTime))
        ).also {
            addTimeEntry(it, database)
        }

        val timeEntryDay =
            TimeEntryDay(0, task2.taskId, LocalDate.now(), Duration.ofMinutes(10)).also {
                addTimeEntryDay(it, database)
            }

        val reportsModel = ReportsModel(taskRepository)
        reportsModel.setSelectedTimeRange(SelectedTimeRange.WEEK)

        composeTestRule.waitForIdle()
        val projectSummaryList = reportsModel.uiState.value.projectSummaries

        assertThat(projectSummaryList, hasSize(2))

        val projectIdProjectMap = projectSummaryList.map { Pair(it.projectId, it) }.toMap()

        assertThat(
            projectIdProjectMap[project.projectId]!!.duration,
            equalTo(Duration.between(startTime, stopTime))
        )
        assertThat(
            projectIdProjectMap[project2.projectId]!!.duration,
            equalTo(Duration.ofMinutes(10))
        )
    }


}