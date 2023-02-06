package com.kjipo.timetracker.database

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE
import java.time.Instant

@Entity(foreignKeys = arrayOf(
    ForeignKey(entity = Task::class,
        parentColumns = ["taskId"],
        childColumns = ["taskId"],
        onDelete = CASCADE)
)
)
data class TimeEntry(
    @PrimaryKey(autoGenerate = true) var timeEntryId: Long = 0,
    val taskId: Long,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var start: Instant,
    var stop: Instant? = null
)


@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) var taskId: Long = 0,
    var title: String
)

@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) var projectId: Long = 0,
    var title: String
)

@Entity(primaryKeys = ["timeEntryId", "taskId"])
data class TimeEntryTaskCrossRef(
    val timeEntryId: Long,
    val taskId: Long
)


@Entity(
    primaryKeys = ["projectId", "taskId"],
    indices = [Index("taskId")]
)
data class ProjectTasksCrossRef(
    val projectId: Long,
    val taskId: Long
)


data class TaskWithTimeEntries(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId",
    )
    val timeEntries: List<TimeEntry>
)

data class ProjectWithTaskEntries(
    @Embedded val project: Project,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "taskId",
        associateBy = Junction(ProjectTasksCrossRef::class)
    )
    val taskEntries: List<Task>
)