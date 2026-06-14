package com.kjipo.timetracker.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.kjipo.timetracker.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.zip.ZipInputStream

class BackupImporter(
    private val database: AppDatabase,
    private val context: Context
) {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun restoreFromUri(uri: Uri) = withContext(Dispatchers.IO) {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                var content: String? = null
                while (entry != null) {
                    if (entry.name == "backup.json") {
                        content = zip.bufferedReader().readText()
                        break
                    }
                    entry = zip.nextEntry
                }
                content
            }
        } ?: throw IllegalArgumentException("Could not open input stream for URI: $uri")

        if (jsonString == null) {
            throw IllegalArgumentException("Selected file does not contain backup.json")
        }

        val backupFile = json.decodeFromString<BackupFile>(jsonString)

        validateManifest(backupFile.manifest)

        database.withTransaction {
            val backupDao = database.backupDao()

            // Delete existing data in correct order
            backupDao.deleteTimeEntryTaskCrossRefs()
            backupDao.deleteTagTaskCrossRefs()
            backupDao.deleteTimeEntries()
            backupDao.deleteTimeEntryDays()
            backupDao.deleteSprintDays()
            backupDao.deleteCustomDays()
            backupDao.deleteTasks()
            backupDao.deleteTags()
            backupDao.deleteProjects()
            backupDao.deleteSprints()
            backupDao.deleteDayTypes()

            // Insert restored data in dependency order
            backupDao.insertProjects(backupFile.data.projects.map { it.toEntity() })
            backupDao.insertTags(backupFile.data.tags.map { it.toEntity() })
            backupDao.insertDayTypes(backupFile.data.dayTypes.map { it.toEntity() })
            backupDao.insertSprints(backupFile.data.sprints.map { it.toEntity() })
            backupDao.insertTasks(backupFile.data.tasks.map { it.toEntity() })
            backupDao.insertTimeEntries(backupFile.data.timeEntries.map { it.toEntity() })
            backupDao.insertTimeEntryDays(backupFile.data.timeEntryDays.map { it.toEntity() })
            backupDao.insertSprintDays(backupFile.data.sprintDays.map { it.toEntity() })
            backupDao.insertCustomDays(backupFile.data.customDays.map { it.toEntity() })
            backupDao.insertTimeEntryTaskCrossRefs(backupFile.data.timeEntryTaskCrossRefs.map { it.toEntity() })
            backupDao.insertTagTaskCrossRefs(backupFile.data.tagTaskCrossRefs.map { it.toEntity() })
        }
    }

    private fun validateManifest(manifest: BackupManifest) {
        if (manifest.formatVersion != 1) {
            throw IllegalArgumentException("Unsupported backup format version: ${manifest.formatVersion}")
        }
        if (manifest.appName != "TimeTracker") {
            // We could be more lenient here but let's follow the suggested policy
            throw IllegalArgumentException("Unsupported app name in backup: ${manifest.appName}")
        }
    }

    private fun ProjectBackupDto.toEntity() = Project(
        projectId = projectId,
        title = title,
        colour = colour?.let { android.graphics.Color.valueOf(it) }
    )

    private fun TagBackupDto.toEntity() = Tag(
        tagId = tagId,
        title = title,
        colour = colour?.let { android.graphics.Color.valueOf(it) }
    )

    private fun DayTypeBackupDto.toEntity() = DayType(
        dayTypeId = dayTypeId,
        title = title,
        workingHours = workingHours
    )

    private fun SprintBackupDto.toEntity() = Sprint(
        sprintId = sprintId,
        title = title,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate)
    )

    private fun TaskBackupDto.toEntity() = Task(
        taskId = taskId,
        title = title,
        projectId = projectId,
        lastUpdated = lastUpdated?.let { Instant.parse(it) },
        closed = closed
    )

    private fun TimeEntryBackupDto.toEntity() = TimeEntry(
        timeEntryId = timeEntryId,
        taskId = taskId,
        start = Instant.parse(start),
        stop = stop?.let { Instant.parse(it) }
    )

    private fun TimeEntryDayBackupDto.toEntity() = TimeEntryDay(
        id = id,
        taskId = taskId,
        date = LocalDate.parse(date),
        duration = Duration.ofMillis(durationMillis)
    )

    private fun SprintDayBackupDto.toEntity() = SprintDay(
        sprintDayId = sprintDayId,
        sprintId = sprintId,
        dayTypeId = dayTypeId,
        date = LocalDate.parse(date)
    )

    private fun CustomDayBackupDto.toEntity() = CustomDay(
        customDayId = customDayId,
        sprintId = sprintId,
        date = LocalDate.parse(date),
        workingHours = workingHours
    )

    private fun TimeEntryTaskCrossRefBackupDto.toEntity() = TimeEntryTaskCrossRef(
        timeEntryId = timeEntryId,
        taskId = taskId
    )

    private fun TagTasksCrossRefBackupDto.toEntity() = TagTasksCrossRef(
        tagId = tagId,
        taskId = taskId
    )
}
