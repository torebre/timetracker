package com.kjipo.timetracker

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.Converters
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TagTasksCrossRef
import com.kjipo.timetracker.database.Task
import com.kjipo.timetracker.database.TimeEntry
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneOffset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppContainerImpl(applicationContext)


        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

            // Add some test data when running in debug

//            lifecycleScope.launch(Dispatchers.IO) {
//                appContainer.appDatabase.clearAllTables()
//                addTestData(appContainer.appDatabase)
//            }
        }

        setContent {
            TimeTrackerApp(appContainer)
        }
    }


    private fun addTestData(appDatabase: AppDatabase) {
        val converters = Converters()

        val tag =
            Tag(0, "Project 1", Color.valueOf(Color.RED)).also { addTag(it, appDatabase) }
        val tag2 =
            Tag(0, "Project 2", Color.valueOf(Color.GREEN)).also { addTag(it, appDatabase) }
        val tag3 =
            Tag(0, "Project 3", Color.valueOf(Color.BLUE)).also { addTag(it, appDatabase) }

        val task = Task(0, "Task 1").also { addTask(it, appDatabase) }
        val task2 = Task(0, "Task 2").also { addTask(it, appDatabase) }
        val task3 = Task(0, "Task 3").also { addTask(it, appDatabase) }

        val timeEntry = TimeEntry(
            0, task.taskId, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                ZoneOffset.UTC
            ),
            LocalDateTime.of(2023, 1, 6, 12, 0, 5).toInstant(
                ZoneOffset.UTC
            )
        ).also {
            addTimeEntry(it, appDatabase)
        }
        val timeEntry2 = TimeEntry(
            0, task.taskId, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                ZoneOffset.UTC
            )
        ).also {
            addTimeEntry(it, appDatabase)
        }
        val timeEntry3 = TimeEntry(
            0, task2.taskId, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                ZoneOffset.UTC
            )
        ).also {
            addTimeEntry(it, appDatabase)
        }

        addTagToTask(tag, task, appDatabase)
        addTagToTask(tag2, task, appDatabase)
        addTagToTask(tag3, task3, appDatabase)

        val tasksWithTimeEntries = appDatabase.taskDao().getTasksWithTimeEntries()

        Timber.tag("Task").i("Tasks with time entries: ${tasksWithTimeEntries.size}")

    }


    private fun addTag(tag: Tag, appDatabase: AppDatabase) {
        tag.tagId = appDatabase.tagDao().insertTag(tag)
    }

    private fun addTagToTask(tag: Tag, task: Task, appDatabase: AppDatabase) {
        appDatabase.taskDao().insertTaskAndTagCrossRef(TagTasksCrossRef(tag.tagId, task.taskId))
    }

    private fun addTask(task: Task, appDatabase: AppDatabase) {
        task.taskId = appDatabase.taskDao().insertTask(task)
    }

    private fun addTimeEntry(timeEntry: TimeEntry, appDatabase: AppDatabase) {
        timeEntry.timeEntryId = appDatabase.timeEntryDao().insertTimeEntry(timeEntry)
    }
}