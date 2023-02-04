package com.kjipo.timetracker.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import com.kjipo.timetracker.database.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskListModel(taskRepository: TaskRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(TaskListUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update {
                viewModelState.value.copy(tasks = taskRepository.getTasks())
            }
        }
    }


        companion object {

            fun provideFactory(
                taskRepository: TaskRepository
            ): ViewModelProvider.Factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return TaskListModel(taskRepository) as T
                    }
                }
        }

    }


    data class TaskListUiState(val tasks: List<Task> = emptyList()) {


    }