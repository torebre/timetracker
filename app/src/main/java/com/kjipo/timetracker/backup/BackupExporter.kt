package com.kjipo.timetracker.backup

import android.content.Context
import android.net.Uri
import com.kjipo.timetracker.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupExporter(
    private val database: AppDatabase,
    private val context: Context
) {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun exportToUri(uri: Uri) = withContext(Dispatchers.IO) {
        val backupDao = database.backupDao()

        val data = BackupData(
            timeEntries = backupDao.getTimeEntries().map { it.toBackupDto() },
            tasks = backupDao.getTasks().map { it.toBackupDto() },
            tags = backupDao.getTags().map { it.toBackupDto() },
            timeEntryTaskCrossRefs = backupDao.getTimeEntryTaskCrossRefs().map { it.toBackupDto() },
            tagTaskCrossRefs = backupDao.getTagTasksCrossRefs().map { it.toBackupDto() },
            timeEntryDays = backupDao.getTimeEntryDays().map { it.toBackupDto() },
            projects = backupDao.getProjects().map { it.toBackupDto() },
            sprints = backupDao.getSprints().map { it.toBackupDto() },
            dayTypes = backupDao.getDayTypes().map { it.toBackupDto() },
            sprintDays = backupDao.getSprintDays().map { it.toBackupDto() },
            customDays = backupDao.getCustomDays().map { it.toBackupDto() }
        )

        val manifest = BackupManifest(
            formatVersion = 1,
            appName = "TimeTracker",
            createdAt = Instant.now().toString(),
            databaseVersion = database.openHelper.readableDatabase.version
        )

        val backupFile = BackupFile(manifest, data)
        val jsonString = json.encodeToString(backupFile)
        val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { zip ->
                zip.putNextEntry(ZipEntry("backup.json"))
                zip.write(jsonBytes)
                zip.closeEntry()
            }
        } ?: throw Exception("Could not open output stream for URI: $uri")
    }

    private fun TimeEntry.toBackupDto() = TimeEntryBackupDto(
        timeEntryId = timeEntryId,
        taskId = taskId,
        start = start.toString(),
        stop = stop?.toString()
    )

    private fun Task.toBackupDto() = TaskBackupDto(
        taskId = taskId,
        title = title,
        projectId = projectId,
        lastUpdated = lastUpdated?.toString(),
        closed = closed
    )

    private fun Tag.toBackupDto() = TagBackupDto(
        tagId = tagId,
        title = title,
        colour = colour?.toArgb()
    )

    private fun TimeEntryTaskCrossRef.toBackupDto() = TimeEntryTaskCrossRefBackupDto(
        timeEntryId = timeEntryId,
        taskId = taskId
    )

    private fun TagTasksCrossRef.toBackupDto() = TagTasksCrossRefBackupDto(
        tagId = tagId,
        taskId = taskId
    )

    private fun TimeEntryDay.toBackupDto() = TimeEntryDayBackupDto(
        id = id,
        taskId = taskId,
        date = date.toString(),
        durationMillis = duration.toMillis()
    )

    private fun Project.toBackupDto() = ProjectBackupDto(
        projectId = projectId,
        title = title,
        colour = colour?.toArgb()
    )

    private fun Sprint.toBackupDto() = SprintBackupDto(
        sprintId = sprintId,
        title = title,
        startDate = startDate.toString(),
        endDate = endDate.toString()
    )

    private fun DayType.toBackupDto() = DayTypeBackupDto(
        dayTypeId = dayTypeId,
        title = title,
        workingHours = workingHours
    )

    private fun SprintDay.toBackupDto() = SprintDayBackupDto(
        sprintDayId = sprintDayId,
        sprintId = sprintId,
        dayTypeId = dayTypeId,
        date = date.toString()
    )

    private fun CustomDay.toBackupDto() = CustomDayBackupDto(
        customDayId = customDayId,
        sprintId = sprintId,
        date = date.toString(),
        workingHours = workingHours
    )
}
