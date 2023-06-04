package com.kjipo.timetracker.taskscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant


class TaskScreenModel(
    @Volatile private var taskId: Long,
    private val taskRepository: TaskRepository
) :
    ViewModel() {

    private val viewModelState = MutableStateFlow(TaskScreenUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    init {
        if (taskId != 0L) {
            // The tasks already exists, load the data for it
            viewModelScope.launch(Dispatchers.IO) {
                loadTask()
            }
        } else {
            // The task does not exist yet
            viewModelScope.launch(Dispatchers.IO) {
                // All tags are available for selection when the task is new
                val availableTags = taskRepository.getTags()
                    .map { TaskMarkUiElement(it) }
                val projects = taskRepository.getProjects()
                    .map { TaskMarkUiElement(it) }
                viewModelState.update {
                    it.copy(
                        initialLoading = false,
                        availableTags = availableTags,
                        availableProjects = projects
                    )
                }
            }
        }
    }


    fun saveTask(taskName: String, tags: List<TaskMarkUiElement>, project: TaskMarkUiElement?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (taskId == 0L) {
                taskId = taskRepository.createTask(taskName, tags.map { it.toTag() }, project?.toProject()).taskId
            } else {
                taskRepository.saveTask(
                    taskId,
                    taskName,
                    tags.map { it.elementId },
                    project?.elementId
                )
            }
            loadTask()
        }
    }

    private suspend fun loadTask() {
//        taskRepository.getTags().forEach { tag ->
//            taskRepository.getTasksForTag(tag.tagId).forEach { tagWithTaskEntries ->
//                Timber.tag("TaskScreenModel").i(
//                    "Tag: ${tagWithTaskEntries.tag.title}. Task entries: ${
//                        tagWithTaskEntries.taskEntries.map { it.title }.joinToString(",")
//                    }"
//                )
//            }
//        }

        taskRepository.getTaskWithTimeEntries(taskId)?.let { taskWithTimeEntries ->
            val tagIds = taskWithTimeEntries.tags.map { it.tagId }
            val availableTags = taskRepository.getTags()
                .filter { !tagIds.contains(it.tagId) }
                .map { TaskMarkUiElement(it) }
            val availableProjects = taskRepository.getProjects()
                .map { TaskMarkUiElement(it) }

            Timber.tag("TaskScreenModel").i("Projects: ${availableProjects}")

            viewModelState.update { taskScreenUiState ->
                taskScreenUiState.copy(
                    taskUi = TaskUi(taskWithTimeEntries),
                    timeEntries = taskWithTimeEntries.timeEntries.map { TimeEntryUi(it) },
                    tags = taskWithTimeEntries.tags.map { TaskMarkUiElement(it) },
                    initialLoading = false,
                    availableTags = availableTags,
                    availableProjects = availableProjects,
                    project = taskWithTimeEntries.project?.let { TaskMarkUiElement(it) }
                )
            }
        }
    }

    fun removeTag(tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.removeTag(taskId, tagId)
            loadTask()
        }

    }

    fun addTag(tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.addTag(taskId, tagId)
            loadTask()
        }
    }

    fun deleteTimeEntry(timeEntryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTimeEntry(timeEntryId)
            loadTask()
        }
    }

    companion object {

        fun provideFactory(
            taskId: Long,
            taskRepository: TaskRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskScreenModel(taskId, taskRepository) as T
                }
            }
    }


}

data class TaskUi(
    val taskId: Long = 0,
    val taskName: String = ""
) {

    constructor(taskWithTimeEntries: TaskWithTimeEntries) : this(
        taskWithTimeEntries.task.taskId,
        taskWithTimeEntries.task.title
    )

}

data class TimeEntryUi(
    val id: Long,
    var start: Instant,
    var stop: Instant? = null
) {

    constructor(timeEntry: TimeEntry) : this(timeEntry.timeEntryId, timeEntry.start, timeEntry.stop)

}

data class TaskScreenUiState(
    val taskUi: TaskUi = TaskUi(),
    val timeEntries: List<TimeEntryUi> = emptyList(),
    val tags: List<TaskMarkUiElement> = emptyList(),
    val initialLoading: Boolean = true,
    val availableTags: List<TaskMarkUiElement> = emptyList(),
    val availableProjects: List<TaskMarkUiElement> = emptyList(),
    val project: TaskMarkUiElement? = null
)