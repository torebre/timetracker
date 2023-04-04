package com.kjipo.timetracker.taskscreen

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.tagscreen.toAndroidGraphicsColor
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
                    .map { TagUi(it) }
                viewModelState.update {
                    it.copy(
                        initialLoading = false,
                        availableTags = availableTags
                    )
                }
            }
        }
    }


    fun saveTask(taskName: String, tags: List<TagUi>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (taskId == 0L) {
                taskId = taskRepository.createTask(taskName, tags.map { it.toTag() }).taskId
            } else {
                taskRepository.saveTask(taskId, taskName, tags.map { it.tagId })
            }
            loadTask()
        }
    }

    private suspend fun loadTask() {
        taskRepository.getTags().forEach { tag ->
            taskRepository.getTasksForTag(tag.tagId).forEach { tagWithTaskEntries ->
                Timber.tag("TaskScreenModel").i(
                    "Tag: ${tagWithTaskEntries.tag.title}. Task entries: ${
                        tagWithTaskEntries.taskEntries.map { it.title }.joinToString(",")
                    }"
                )
            }
        }

        taskRepository.getTaskWithTimeEntries(taskId)?.let { taskWithTimeEntries ->
            val tagIds = taskWithTimeEntries.tags.map { it.tagId }
            val availableTags = taskRepository.getTags()
                .filter { !tagIds.contains(it.tagId) }
                .map { TagUi(it) }

            Timber.tag("TaskScreenModel").i("Tags: ${tagIds}")

            viewModelState.update { taskScreenUiState ->
                taskScreenUiState.copy(
                    taskUi = TaskUi(taskWithTimeEntries),
                    timeEntries = taskWithTimeEntries.timeEntries.map { TimeEntryUi(it) },
                    tags = taskWithTimeEntries.tags.map { TagUi(it) },
                    initialLoading = false,
                    availableTags = availableTags
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


data class TagUi(val tagId: Long, val title: String, val colour: Color?) {

    constructor(tag: Tag) : this(tag.tagId, tag.title, tag.colour?.let {
        Color(it.red(), it.green(), it.blue())
    })

    fun toTag() = Tag(tagId = tagId, title = title, colour = colour?.toAndroidGraphicsColor())

}


data class TaskScreenUiState(
    val taskUi: TaskUi = TaskUi(),
    val timeEntries: List<TimeEntryUi> = emptyList(),
    val tags: List<TagUi> = emptyList(),
    val initialLoading: Boolean = true,
    val availableTags: List<TagUi> = emptyList()
)