package com.kjipo.timetracker.database

import androidx.room.Transaction
import com.kjipo.timetracker.tasklist.SortOrder
import java.time.Duration
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDateTime
import java.time.ZoneId


interface TaskRepository {

    suspend fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries?
    suspend fun getTasks(): List<Task>

    suspend fun getTask(taskId: Long): Task?

    suspend fun createTask(
        title: String = "",
        tags: List<Tag> = emptyList(),
        project: Project?
    ): Task

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun getTasksWithTimeEntries(
        startTime: LocalDateTime? = null,
        stopTime: LocalDateTime? = null,
        sortOrder: SortOrder = SortOrder.DEFAULT
    ): List<TaskWithTimeEntries>

    suspend fun addTimeEntry(timeEntry: TimeEntry)

    suspend fun setStopForTimeEntry(timeEntryId: Long, stop: Instant)

    suspend fun getTimeEntry(timeEntryId: Long): TimeEntry?

    suspend fun updateTimeEntry(timeEntry: TimeEntry)

    suspend fun saveTask(taskId: Long, taskName: String, tagIds: List<Long>, projectId: Long?)

    suspend fun getTags(): List<Tag>

    suspend fun getTag(id: Long): Tag?

    suspend fun insertTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)

    suspend fun deleteTag(tag: Tag)

    suspend fun addTag(taskId: Long, tagId: Long)

    suspend fun removeTag(taskId: Long, tagId: Long)

    suspend fun getTasksForTag(tagId: Long): List<TagWithTaskEntries>

    suspend fun deleteTimeEntry(timeEntryId: Long)

    suspend fun getProjects(): List<Project>

    suspend fun getProject(projectId: Long): Project?

    suspend fun insertProject(project: Project): Long

    suspend fun updateProject(project: Project)

    suspend fun deleteProject(project: Project)

    suspend fun addProject(taskId: Long, projectId: Long)

    suspend fun removeProject(taskId: Long, projectId: Long)

    suspend fun updateTimeEntry(timeEntry: Long, start: Instant, stop: Instant?): TimeEntry?

}


class TaskRepositoryImpl(private val appDatabase: AppDatabase) : TaskRepository {

    override suspend fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries? {
        return appDatabase.taskDao().getTaskWithTimeEntries(taskId)
    }

    override suspend fun getTasks(): List<Task> {
        return appDatabase.taskDao().getTasks()
    }

    override suspend fun getTask(taskId: Long): Task? {
        return appDatabase.taskDao().getTask(taskId)
    }

    @Transaction
    override suspend fun createTask(title: String, tags: List<Tag>, project: Project?): Task {
        val newTask = Task(0, title, projectId = project?.projectId, lastUpdated = now())
        appDatabase.taskDao().insertTask(newTask).also { newTask.taskId = it }
        for (tag in tags) {
            addTag(newTask.taskId, tag.tagId)
        }

        return newTask
    }

    override suspend fun updateTask(task: Task) {
        appDatabase.taskDao().updateTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        appDatabase.taskDao().deleteTask(task)
    }

    override suspend fun getTasksWithTimeEntries(
        startTime: LocalDateTime?,
        stopTime: LocalDateTime?,
        sortOrder: SortOrder
    ): List<TaskWithTimeEntries> {
        val sortColumn = when (sortOrder) {
            SortOrder.DEFAULT -> "id"
            SortOrder.RECENTLY_USED -> "lastUpdated"
        }
        val taskWithTimeEntries = appDatabase.taskDao().getTasksWithTimeEntries(sortColumn)

        if (startTime == null && stopTime == null) {
            return taskWithTimeEntries
        }

        return taskWithTimeEntries.filter {
            shouldTimeEntryBeIncluded(it, startTime, stopTime)
        }
    }


    override suspend fun addTimeEntry(timeEntry: TimeEntry) {
        appDatabase.timeEntryDao().insertTimeEntry(timeEntry)
    }

    override suspend fun setStopForTimeEntry(timeEntryId: Long, stop: Instant) {
        appDatabase.timeEntryDao().getTimeEntry(timeEntryId)?.let { timeEntry ->
            appDatabase.timeEntryDao().updateTimeEntry(timeEntry.copy(stop = stop))
        }
    }

    override suspend fun getTimeEntry(timeEntryId: Long): TimeEntry? {
        return appDatabase.timeEntryDao().getTimeEntry(timeEntryId)
    }

    override suspend fun updateTimeEntry(timeEntry: TimeEntry) {
        appDatabase.timeEntryDao().updateTimeEntry(timeEntry)
    }

    override suspend fun updateTimeEntry(
        timeEntryId: Long,
        start: Instant,
        stop: Instant?
    ): TimeEntry? {
        return appDatabase.timeEntryDao().getTimeEntry(timeEntryId)?.let { timeEntry ->

            appDatabase.taskDao().getTask(timeEntry.taskId)?.let { task ->
                appDatabase.taskDao().updateLastActiveForTask(task)
            }

            timeEntry.start = start
            timeEntry.stop = stop
            appDatabase.timeEntryDao().updateTimeEntry(timeEntry)
            timeEntry
        }
    }

    @Transaction
    override suspend fun saveTask(
        taskId: Long,
        taskName: String,
        tagIds: List<Long>,
        projectId: Long?
    ) {
        appDatabase.taskDao().getTaskWithTimeEntries(taskId)?.let { taskWithTimeEntries ->
            appDatabase.taskDao()
                .updateTask(taskWithTimeEntries.task.copy(title = taskName, projectId = projectId))

            val tagsToRemove = taskWithTimeEntries.tags.filter { !tagIds.contains(it.tagId) }
            tagsToRemove.forEach { tag ->
                appDatabase.taskDao().removeTaskAndTagCrossRef(TagTasksCrossRef(tag.tagId, taskId))
            }

            val existingTags = taskWithTimeEntries.tags.map { it.tagId }.toSet()
            tagIds.forEach { tagId ->
                if (!existingTags.contains(tagId)) {
                    appDatabase.taskDao().insertTaskAndTagCrossRef(TagTasksCrossRef(tagId, taskId))
                }
            }
        }

        appDatabase.taskDao().getTask(taskId)?.let { task ->
            appDatabase.taskDao().updateLastActiveForTask(task)
        }
    }

    override suspend fun getTags(): List<Tag> {
        return appDatabase.tagDao().getTags()
    }

    override suspend fun getTag(id: Long): Tag? {
        return appDatabase.tagDao().getTag(id)
    }

    override suspend fun insertTag(tag: Tag): Long {
        return appDatabase.tagDao().insertTag(tag)
    }

    override suspend fun updateTag(tag: Tag) {
        appDatabase.tagDao().updateTag(tag)
    }

    override suspend fun deleteTag(tag: Tag) {
        appDatabase.tagDao().deleteTag(tag)
    }

    override suspend fun addTag(taskId: Long, tagId: Long) {
        appDatabase.taskDao().insertTaskAndTagCrossRef(TagTasksCrossRef(tagId, taskId))
    }

    override suspend fun removeTag(taskId: Long, tagId: Long) {
        appDatabase.taskDao().removeTaskAndTagCrossRef(TagTasksCrossRef(tagId, taskId))
    }

    override suspend fun getTasksForTag(tagId: Long): List<TagWithTaskEntries> {
        return appDatabase.tagDao().getTasksForTag(tagId)
    }

    override suspend fun deleteTimeEntry(timeEntryId: Long) {
        appDatabase.timeEntryDao().getTimeEntry(timeEntryId)?.apply {
            appDatabase.timeEntryDao().deleteTimeEntry(this)
        }
    }

    override suspend fun getProjects(): List<Project> {
        return appDatabase.projectDao().getProjects()
    }

    override suspend fun getProject(projectId: Long): Project? {
        return appDatabase.projectDao().getProject(projectId)
    }

    override suspend fun insertProject(project: Project): Long {
        return appDatabase.projectDao().insertProject(project)
    }

    override suspend fun updateProject(project: Project) {
        appDatabase.projectDao().updateProject(project)
    }

    override suspend fun deleteProject(project: Project) {
        appDatabase.projectDao().deleteProject(project)
    }

    override suspend fun addProject(taskId: Long, projectId: Long) {
        TODO("Not yet implemented")

    }

    override suspend fun removeProject(taskId: Long, projectId: Long) {
        TODO("Not yet implemented")
    }

    companion object {

        internal fun shouldTimeEntryBeIncluded(
            task: TaskWithTimeEntries,
            startTime: LocalDateTime?,
            stopTime: LocalDateTime?
        ): Boolean {
            return task.timeEntriesDay.any { timeEntryDay ->
                shouldTimeEntryDayBeIncluded(timeEntryDay, startTime, stopTime)
            } ||
                    task.timeEntries.any { timeEntry ->
                        shouldTimeEntryBeIncluded(timeEntry, startTime, stopTime)
                    }

        }

        internal fun shouldTimeEntryBeIncluded(
            timeEntry: TimeEntry,
            startTime: LocalDateTime?,
            stopTime: LocalDateTime?
        ): Boolean {
            val duration = timeEntry.getDuration()
                ?: Duration.ofSeconds(Instant.now().epochSecond - timeEntry.start.epochSecond)

            // TODO Be more consistent in using Instant or LocalDateTime
            val timeEntryStop = LocalDateTime.ofEpochSecond(
                timeEntry.start.epochSecond + duration.seconds,
                0,
                ZoneId.systemDefault().rules.getOffset(Instant.now())
            )
            val timeEntryStart = LocalDateTime.ofInstant(
                timeEntry.start,
                ZoneId.systemDefault().rules.getOffset(Instant.now())
            )

            return if (startTime == null) {
                stopTime?.isAfter(timeEntryStop) ?: true
            } else {
                if (stopTime == null) {
                    startTime.isBefore(timeEntryStart)
                } else {
                    timeEntryStart.isAfter(startTime) && timeEntryStop.isBefore(stopTime)
                }
            }
        }


        internal fun shouldTimeEntryDayBeIncluded(
            timeEntryDay: TimeEntryDay,
            startTime: LocalDateTime?,
            stopTime: LocalDateTime?
        ): Boolean {
            return if (startTime == null) {
                stopTime?.isAfter(timeEntryDay.date.atStartOfDay()) ?: true
            } else {
                if (stopTime == null) {
                    startTime.isBefore(timeEntryDay.date.atStartOfDay())
                } else {
                    stopTime.isAfter(timeEntryDay.date.atStartOfDay()) &&
                            startTime.isBefore(timeEntryDay.date.atTime(23, 59, 59))
                }
            }
        }
    }


}