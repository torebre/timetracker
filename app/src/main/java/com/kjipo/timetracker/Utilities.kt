package com.kjipo.timetracker

import android.graphics.Color
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TagTasksCrossRef
import com.kjipo.timetracker.database.Task
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.database.TimeEntryDay
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random


val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E d. M")
val weekDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d. M")
val reportDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("kk:mm:ss")
val exportDateFormat: DateTimeFormatter = DateTimeFormatter.ISO_DATE


fun Duration.toMinutesPartHelper() = toMinutes() % 60

fun Duration.toSecondsPartHelper() = seconds % 60


fun toTwoDigits(value: Long): String {
    return if (value < 10) {
        "0${value}"
    } else {
        "$value"
    }
}


suspend fun addTestData(appDatabase: AppDatabase) {
    val project =
        Project(0, "Project 1", Color.valueOf(Color.GREEN)).let { addProject(it, appDatabase); it }
    val project2 =
        Project(0, "Project 2", Color.valueOf(Color.YELLOW)).let { addProject(it, appDatabase); it }
    val project3 =
        Project(0, "Project 3", Color.valueOf(Color.RED)).let { addProject(it, appDatabase); it }

    val tag =
        Tag(0, "Tag 1", Color.valueOf(Color.RED)).let { addTag(it, appDatabase); it }
    val tag2 =
        Tag(0, "Tag 2", Color.valueOf(Color.GREEN)).let { addTag(it, appDatabase); it }
    val tag3 =
        Tag(0, "Tag 3", Color.valueOf(Color.BLUE)).let { addTag(it, appDatabase); it }

    val task = Task(0, "Task 1").let { addTask(it, appDatabase); it }
        .also { setProjectForTask(project, it, appDatabase) }
    val task2 = Task(0, "Task 2").let { addTask(it, appDatabase); it }
    val task3 = Task(0, "Task 3").let { addTask(it, appDatabase); it }
        .also { setProjectForTask(project2, it, appDatabase) }

    val timeEntry = TimeEntry(
        0, task.taskId, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
            ZoneOffset.UTC
        ),
        LocalDateTime.of(2023, 1, 6, 12, 0, 5).toInstant(
            ZoneOffset.UTC
        )
    ).let {
        addTimeEntry(it, appDatabase)
        it
    }
    val timeEntry2 = TimeEntry(
        0, task.taskId, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
            ZoneOffset.UTC
        )
    ).let {
        addTimeEntry(it, appDatabase)
        it
    }
    val timeEntry3 = TimeEntry(
        0, task2.taskId, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
            ZoneOffset.UTC
        )
    ).let {
        addTimeEntry(it, appDatabase)
        it
    }

    val timeEntry4 = TimeEntry(
        0, task.taskId, LocalDateTime.now().minusHours(2).toInstant(ZoneOffset.UTC),
        LocalDateTime.now().toInstant(ZoneOffset.UTC)
    ).let {
        addTimeEntry(it, appDatabase)
        it
    }

    val startTimeEntry5 = LocalDate.now().minusDays(2).atTime(14, 0).toInstant(ZoneOffset.UTC)
    val timeEntry5 = TimeEntry(
        0, task2.taskId, startTimeEntry5,
        startTimeEntry5.plusSeconds(3600)
    ).let {
        addTimeEntry(it, appDatabase)
        it
    }

    val startTimeEntry6 = LocalDate.now().minusDays(4).atTime(15, 0).toInstant(ZoneOffset.UTC)
    val timeEntry6 = TimeEntry(
        0, task3.taskId, startTimeEntry6,
        startTimeEntry6.plusSeconds(7200)
    ).let {
        addTimeEntry(it, appDatabase)
        it
    }

    // The same date as the one above, but with a different task
    val startTimeEntry7 = LocalDate.now().minusDays(4).atTime(15, 0).toInstant(ZoneOffset.UTC)
    val timeEntry7 = TimeEntry(
        0, task2.taskId, startTimeEntry7,
        startTimeEntry7.plusSeconds(7200)
    ).let {
        addTimeEntry(it, appDatabase)
        it
    }

    val timeEntryDay =
        TimeEntryDay(0, task.taskId, LocalDate.of(2023, 2, 5), Duration.ofMinutes(10)).let {
            addTimeEntryDay(it, appDatabase)
            it
        }

    val timeEntryDay2 =
        TimeEntryDay(0, task3.taskId, LocalDate.of(2023, 4, 20), Duration.ofHours(2)).let {
            addTimeEntryDay(it, appDatabase)
            it
        }

    val timeEntryDay3 =
        TimeEntryDay(0, task.taskId, LocalDate.now(), Duration.ofHours(2)).let {
            addTimeEntryDay(it, appDatabase)
            it
        }

    val startTimeEntryDay4 = LocalDate.now().minusDays(1)
    val timeEntryDay4 =
        TimeEntryDay(0, task2.taskId, startTimeEntryDay4, Duration.ofHours(2)).let {
            addTimeEntryDay(it, appDatabase)
            it
        }

    addTagToTask(tag, task, appDatabase)
    addTagToTask(tag2, task, appDatabase)
    addTagToTask(tag3, task3, appDatabase)

    addTimeEntriesToTasks(listOf(task, task2, task3), appDatabase)
}

private suspend fun addTimeEntriesToTasks(tasks: Collection<Task>, appDatabase: AppDatabase) {
    val random = Random(1)

    for (task in tasks) {
        var current = Instant.now().minusSeconds(random.nextLong(600, 3600))

        for (i in 0 until 10) {
            val duration = Duration.ofSeconds(random.nextLong(600, 3600))
            LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                ZoneOffset.UTC
            )

            addTimeEntry(
                TimeEntry(
                    0, task.taskId, current.minus(duration), current
                ), appDatabase
            )

            current = current.minus(duration).minusSeconds(random.nextLong(600, 3600))
        }
    }

}


suspend fun addProject(project: Project, appDatabase: AppDatabase) {
    project.projectId = appDatabase.projectDao().insertProject(project)
}

suspend fun setProjectForTask(project: Project, task: Task, appDatabase: AppDatabase) {
    appDatabase.taskDao().updateTask(task.copy(projectId = project.projectId))
}

suspend fun addTag(tag: Tag, appDatabase: AppDatabase) {
    tag.tagId = appDatabase.tagDao().insertTag(tag)
}

suspend fun addTagToTask(tag: Tag, task: Task, appDatabase: AppDatabase) {
    appDatabase.taskDao().insertTaskAndTagCrossRef(TagTasksCrossRef(tag.tagId, task.taskId))
}

suspend fun addTask(task: Task, appDatabase: AppDatabase) {
    task.taskId = appDatabase.taskDao().insertTask(task)
}

suspend fun addTimeEntry(timeEntry: TimeEntry, appDatabase: AppDatabase) {
    timeEntry.timeEntryId = appDatabase.timeEntryDao().insertTimeEntry(timeEntry)
}

suspend fun addTimeEntryDay(timeEntryDay: TimeEntryDay, appDatabase: AppDatabase) {
    timeEntryDay.id = appDatabase.timeEntryDao().insertTimeEntryDay(timeEntryDay)
}

fun formatDuration(duration: Duration): String {
    with(duration) {
        val hours = duration.toHours()
        val minutes = toTwoDigits(toMinutesPartHelper())
        val seconds = toTwoDigits(toSecondsPartHelper())

        return "$hours:$minutes:$seconds"
    }
}
