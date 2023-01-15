package com.kjipo.timetracker.database

import androidx.room.*
import java.time.Instant

@Entity
data class TimeEntry(
    @PrimaryKey(autoGenerate = true) var timeEntryId: Long = 0,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var start: Instant,
    var stop: Instant? = null
)

@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) var projectId: Long = 0,
    var title: String
)

@Entity(primaryKeys = ["timeEntryId", "projectId"])
data class TimeEntryProjectCrossRef(
    val timeEntryId: Long,
    val projectId: Long
)

data class ProjectWithTimeEntries(
    @Embedded val project: Project,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "timeEntryId",
        associateBy = Junction(TimeEntryProjectCrossRef::class)
    )
    val timeEntries: List<TimeEntry>
)