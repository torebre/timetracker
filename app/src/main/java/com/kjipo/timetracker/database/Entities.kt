package com.kjipo.timetracker.database

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.SET_NULL
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

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
) {

    fun getDuration(): Duration? {
        return stop?.let {
            Duration.between(start, it)
        }
    }

    fun getDurationMissingStopSetToNow(): Duration {
        val timeEntryStop = stop ?: Instant.now()

        return Duration.between(start, timeEntryStop)
    }

}


@Entity(
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["taskId"],
        childColumns = ["taskId"],
        onDelete = CASCADE
    )], indices = [Index("taskId")]
)
data class TimeEntryDay(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val taskId: Long,
    val date: LocalDate,
    val duration: Duration
)


@Entity(
    foreignKeys = [ForeignKey(
        entity = Project::class,
        parentColumns = ["projectId"],
        childColumns = ["projectId"],
        onDelete = SET_NULL
    )], indices = [Index("taskId"), Index("projectId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) var taskId: Long = 0,
    val title: String,
    val projectId: Long? = null,
    val lastUpdated: Instant? = null,
    @ColumnInfo(defaultValue = "0")
    val closed: Boolean = false
)

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) var tagId: Long = 0,
    val title: String,
    val colour: android.graphics.Color? = null
)

@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) var projectId: Long = 0,
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
    val tags: List<Tag>,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val timeEntriesDay: List<TimeEntryDay>,

    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val project: Project?
) {

    fun getTimeEntriesCompletelyWithinInterval(
        startTime: Instant,
        endTime: Instant
    ): List<TimeEntry> {
        return timeEntries.filter {
            it.start.isAfter(startTime) && it.stop?.isBefore(endTime) ?: true
        }
    }

    fun getTimeDayEntriesForDate(localDate: LocalDate): List<TimeEntryDay> {
        return timeEntriesDay.filter { it.date == localDate }
    }


}

@Entity
data class Sprint(
    @PrimaryKey(autoGenerate = true) var sprintId: Long = 0,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

@Entity(
    indices = [Index(value = ["title"], unique = true)]
)
data class DayType(
    @PrimaryKey(autoGenerate = true) var dayTypeId: Long = 0,
    val title: String,
    val workingHours: Double
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Sprint::class,
            parentColumns = ["sprintId"],
            childColumns = ["sprintId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = DayType::class,
            parentColumns = ["dayTypeId"],
            childColumns = ["dayTypeId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("sprintId"), Index("dayTypeId")]
)
data class SprintDay(
    @PrimaryKey(autoGenerate = true) var sprintDayId: Long = 0,
    val sprintId: Long,
    val dayTypeId: Long,
    val date: LocalDate
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Sprint::class,
            parentColumns = ["sprintId"],
            childColumns = ["sprintId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("sprintId")]
)
data class CustomDay(
    @PrimaryKey(autoGenerate = true) var customDayId: Long = 0,
    val sprintId: Long,
    val date: LocalDate,
    val workingHours: Double
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

data class ProjectWithTaskEntries(
    @Embedded val project: Project,
    @Relation(
        entity = Task::class,
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val taskEntries: List<Task>
)

data class ProjectWithTimeEntries(
    @Embedded val project: Project,
    @Relation(
        entity = Task::class,
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val taskEntries: List<TaskWithTimeEntries>
)
