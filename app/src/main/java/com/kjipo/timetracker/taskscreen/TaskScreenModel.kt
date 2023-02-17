package com.kjipo.timetracker.taskscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import com.kjipo.timetracker.database.TimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


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
            viewModelState.update {
                it.copy(
                    taskId = taskWithTimeEntries.task.taskId,
                    taskName = taskWithTimeEntries.task.title,
                    timeEntries = taskWithTimeEntries.timeEntries,
                    initialLoading = false
                )
            }
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


data class TaskScreenUiState(
    val taskId: Long = 0,
    val taskName: String = "",
    val timeEntries: List<TimeEntry> = emptyList(),
    val initialLoading: Boolean = true
)