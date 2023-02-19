package com.kjipo.timetracker.taskscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant


class TaskScreenModel(private var taskId: Long, private val taskRepository: TaskRepository) :
    ViewModel() {

    private val viewModelState = MutableStateFlow(TaskScreenUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    init {
        if (taskId != 0L) {
            viewModelScope.launch(Dispatchers.IO) {
                loadTask()
            }
        } else {
            viewModelState.update { it.copy(initialLoading = false) }
        }
    }


    fun saveTask(taskName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (taskId == 0L) {
                taskId = taskRepository.createTask().taskId
            } else {
                taskRepository.saveTask(taskId, taskName)
            }
            loadTask()
        }
    }

    private suspend fun loadTask() {
        taskRepository.getTaskWithTimeEntries(taskId)?.let { taskWithTimeEntries ->
            viewModelState.update { taskScreenUiState ->
                taskScreenUiState.copy(
                    taskUi = TaskUi(taskWithTimeEntries),
                    timeEntries = taskWithTimeEntries.timeEntries.map { TimeEntryUi(it) },
                    tags = taskWithTimeEntries.tags.map { TagUi(it) },
                    initialLoading = false
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


data class TagUi(val tagId: Long, val title: String) {

    constructor(tag: Tag) : this(tag.tagId, tag.title)

}


data class TaskScreenUiState(
    val taskUi: TaskUi = TaskUi(),
    val timeEntries: List<TimeEntryUi> = emptyList(),
    val tags: List<TagUi> = emptyList(),
    val initialLoading: Boolean = true
)