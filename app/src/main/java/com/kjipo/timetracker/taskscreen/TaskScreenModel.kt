package com.kjipo.timetracker.taskscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class TaskScreenModel(taskRepository: TaskRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(TaskScreenUiState())

    val uiState = viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)



    companion object {

        fun provideFactory(
            taskRepository: TaskRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskScreenModel(taskRepository) as T
                }
            }
    }


}


data class TaskScreenUiState(
    val taskName: String = "",
    val isDirty: Boolean = false
)