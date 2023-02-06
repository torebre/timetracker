package com.kjipo.timetracker.taskscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import com.kjipo.timetracker.database.TimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber


class TaskScreenModel(val taskId: Long, taskRepository: TaskRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(TaskScreenUiState())

    val uiState = viewModelState.stateIn(viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value)


    init {
        viewModelScope.launch(Dispatchers.IO) {
                taskRepository.getTaskWithTimeEntries(taskId)?.let { taskWithTimeEntries ->

                    Timber.tag("Task").i("Number of time entries: ${taskWithTimeEntries.timeEntries.size}")

                    viewModelState.update {
                        TaskScreenUiState(
                            taskWithTimeEntries.task.taskId,
                            taskWithTimeEntries.task.title,
                            taskWithTimeEntries.timeEntries
                        )
                    }
                }
        }
    }





    companion object {

        fun provideFactory(taskId: Long,
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
    val timeEntries: List<TimeEntry> = emptyList()
)