package com.kjipo.timetracker.database

import androidx.compose.ui.graphics.Color
import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE
import java.time.Instant

@Entity(
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["taskId"],
        childColumns = ["taskId"],
        onDelete = CASCADE
    )], indices = [Index("taskId")]
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
    val title: String
)

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) var tagId: Long = 0,
    val title: String,
    val colour: android.graphics.Color? = null
)

@Entity(primaryKeys = ["timeEntryId", "taskId"])
data class TimeEntryTaskCrossRef(
    val timeEntryId: Long,
    val taskId: Long
)


@Entity(
    primaryKeys = ["tagId", "taskId"],
    indices = [Index("taskId")]
)
data class TagTasksCrossRef(
    val tagId: Long,
    val taskId: Long
)


data class TaskWithTimeEntries(
    @Embedded val task: Task,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId",
    )
    val timeEntries: List<TimeEntry>,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "tagId",
        associateBy = Junction(TagTasksCrossRef::class)
    )
    val tags: List<Tag>
)

data class TagWithTaskEntries(
    @Embedded val tag: Tag,
    @Relation(
        parentColumn = "tagId",
        entityColumn = "taskId",
        associateBy = Junction(TagTasksCrossRef::class)
    )
    val taskEntries: List<Task>
)