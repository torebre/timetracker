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
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("kk:mm:ss")
val exportDateFormat: DateTimeFormatter = DateTimeFormatter.ISO_DATE


fun Duration.toHoursPartHelper(): Int {
    return (toHours() % 24).toInt()
}

fun Duration.toMinutesPartHelper(): Int {
    return (toMinutes() % 60).toInt()
}

fun Duration.toSecondsPartHelper(): Int {
    return (seconds % 60).toInt()
}

fun toTwoDigits(value: Int): String {
    return if (value < 10) {
        "0${value}"
    } else {
        "$value"
    }
}


fun addTestData(appDatabase: AppDatabase) {
    val project =
        Project(0, "Project 1", Color.valueOf(Color.GREEN)).also { addProject(it, appDatabase) }
    val project2 =
        Project(0, "Project 2", Color.valueOf(Color.YELLOW)).also { addProject(it, appDatabase) }
    val project3 =
        Project(0, "Project 3", Color.valueOf(Color.RED)).also { addProject(it, appDatabase) }

    val tag =
        Tag(0, "Tag 1", Color.valueOf(Color.RED)).also { addTag(it, appDatabase) }
    val tag2 =
        Tag(0, "Tag 2", Color.valueOf(Color.GREEN)).also { addTag(it, appDatabase) }
    val tag3 =
        Tag(0, "Tag 3", Color.valueOf(Color.BLUE)).also { addTag(it, appDatabase) }

    val task = Task(0, "Task 1").also { addTask(it, appDatabase) }
        .also { setProjectForTask(project, it, appDatabase) }
    val task2 = Task(0, "Task 2").also { addTask(it, appDatabase) }
    val task3 = Task(0, "Task 3").also { addTask(it, appDatabase) }
        .also { setProjectForTask(project2, it, appDatabase) }

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

    val timeEntry4 = TimeEntry(
        0, task.taskId, LocalDateTime.now().minusHours(2).toInstant(ZoneOffset.UTC),
        LocalDateTime.now().toInstant(ZoneOffset.UTC)
    ).also {
        addTimeEntry(it, appDatabase)
    }

    val startTimeEntry5 = LocalDate.now().minusDays(2).atTime(14, 0).toInstant(ZoneOffset.UTC)
    val timeEntry5 = TimeEntry(
        0, task2.taskId, startTimeEntry5,
        startTimeEntry5.plusSeconds(3600)
    ).also {
        addTimeEntry(it, appDatabase)
    }

    val startTimeEntry6 = LocalDate.now().minusDays(4).atTime(15, 0).toInstant(ZoneOffset.UTC)
    val timeEntry6 = TimeEntry(
        0, task3.taskId, startTimeEntry6,
        startTimeEntry6.plusSeconds(7200)
    ).also {
        addTimeEntry(it, appDatabase)
    }

    // The same date as the one above, but with a different task
    val startTimeEntry7 = LocalDate.now().minusDays(4).atTime(15, 0).toInstant(ZoneOffset.UTC)
    val timeEntry7 = TimeEntry(
        0, task2.taskId, startTimeEntry7,
        startTimeEntry7.plusSeconds(7200)
    ).also {
        addTimeEntry(it, appDatabase)
    }

    val timeEntryDay =
        TimeEntryDay(0, task.taskId, LocalDate.of(2023, 2, 5), Duration.ofMinutes(10)).also {
            addTimeEntryDay(it, appDatabase)
        }

    val timeEntryDay2 =
        TimeEntryDay(0, task3.taskId, LocalDate.of(2023, 4, 20), Duration.ofHours(2)).also {
            addTimeEntryDay(it, appDatabase)
        }

    val timeEntryDay3 =
        TimeEntryDay(0, task.taskId, LocalDate.now(), Duration.ofHours(2)).also {
            addTimeEntryDay(it, appDatabase)
        }

    val startTimeEntryDay4 = LocalDate.now().minusDays(1)
    val timeEntryDay4 =
        TimeEntryDay(0, task2.taskId, startTimeEntryDay4, Duration.ofHours(2)).also {
            addTimeEntryDay(it, appDatabase)
        }

    addTagToTask(tag, task, appDatabase)
    addTagToTask(tag2, task, appDatabase)
    addTagToTask(tag3, task3, appDatabase)

    addTimeEntriesToTasks(listOf(task, task2, task3), appDatabase)
}

private fun addTimeEntriesToTasks(tasks: Collection<Task>, appDatabase: AppDatabase) {
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


fun addProject(project: Project, appDatabase: AppDatabase) {
    project.projectId = appDatabase.projectDao().insertProject(project)
}

fun setProjectForTask(project: Project, task: Task, appDatabase: AppDatabase) {
    appDatabase.taskDao().updateTask(task.copy(projectId = project.projectId))
}

fun addTag(tag: Tag, appDatabase: AppDatabase) {
    tag.tagId = appDatabase.tagDao().insertTag(tag)
}

fun addTagToTask(tag: Tag, task: Task, appDatabase: AppDatabase) {
    appDatabase.taskDao().insertTaskAndTagCrossRef(TagTasksCrossRef(tag.tagId, task.taskId))
}

fun addTask(task: Task, appDatabase: AppDatabase) {
    task.taskId = appDatabase.taskDao().insertTask(task)
}

fun addTimeEntry(timeEntry: TimeEntry, appDatabase: AppDatabase) {
    timeEntry.timeEntryId = appDatabase.timeEntryDao().insertTimeEntry(timeEntry)
}

fun addTimeEntryDay(timeEntryDay: TimeEntryDay, appDatabase: AppDatabase) {
    timeEntryDay.id = appDatabase.timeEntryDao().insertTimeEntryDay(timeEntryDay)
}

fun formatDuration(duration: Duration): String {
    with(duration) {
        return "${toTwoDigits(toHoursPartHelper())}:${toTwoDigits(toMinutesPartHelper())}:${
            toTwoDigits(
                toSecondsPartHelper()
            )
        }"
    }
}
