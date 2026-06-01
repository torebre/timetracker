package com.kjipo.timetracker.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupFile(
    val manifest: BackupManifest,
    val data: BackupData
)

@Serializable
data class BackupManifest(
    val formatVersion: Int,
    val appName: String,
    val createdAt: String,
    val databaseVersion: Int
)

@Serializable
data class BackupData(
    val timeEntries: List<TimeEntryBackupDto>,
    val tasks: List<TaskBackupDto>,
    val tags: List<TagBackupDto>,
    val timeEntryTaskCrossRefs: List<TimeEntryTaskCrossRefBackupDto>,
    val tagTaskCrossRefs: List<TagTasksCrossRefBackupDto>,
    val timeEntryDays: List<TimeEntryDayBackupDto>,
    val projects: List<ProjectBackupDto>,
    val sprints: List<SprintBackupDto>,
    val dayTypes: List<DayTypeBackupDto>,
    val sprintDays: List<SprintDayBackupDto>,
    val customDays: List<CustomDayBackupDto>
)

@Serializable
data class TimeEntryBackupDto(
    val timeEntryId: Long,
    val taskId: Long,
    val start: String,
    val stop: String?
)

@Serializable
data class TaskBackupDto(
    val taskId: Long,
    val title: String,
    val projectId: Long?,
    val lastUpdated: String?,
    val closed: Boolean
)

@Serializable
data class TagBackupDto(
    val tagId: Long,
    val title: String,
    val colour: Int?
)

@Serializable
data class TimeEntryTaskCrossRefBackupDto(
    val timeEntryId: Long,
    val taskId: Long
)

@Serializable
data class TagTasksCrossRefBackupDto(
    val tagId: Long,
    val taskId: Long
)

@Serializable
data class TimeEntryDayBackupDto(
    val id: Long,
    val taskId: Long,
    val date: String,
    val durationMillis: Long
)

@Serializable
data class ProjectBackupDto(
    val projectId: Long,
    val title: String,
    val colour: Int?
)

@Serializable
data class SprintBackupDto(
    val sprintId: Long,
    val title: String,
    val startDate: String,
    val endDate: String
)

@Serializable
data class DayTypeBackupDto(
    val dayTypeId: Long,
    val title: String,
    val workingHours: Double
)

@Serializable
data class SprintDayBackupDto(
    val sprintDayId: Long,
    val sprintId: Long,
    val dayTypeId: Long,
    val date: String
)

@Serializable
data class CustomDayBackupDto(
    val customDayId: Long,
    val sprintId: Long,
    val date: String,
    val workingHours: Double
)
