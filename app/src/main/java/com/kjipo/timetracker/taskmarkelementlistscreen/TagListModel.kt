package com.kjipo.timetracker.taskmarkelementlistscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskMarkerModel(private val isTag: Boolean, private val taskRepository: TaskRepository) :
    ViewModel() {
    private val viewModelState = MutableStateFlow(TagListUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    init {
        reload()
    }

    fun reload() {
        if (isTag) {
            loadTags()
        } else {
            loadProjects()
        }
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update { tagListUiState ->
                tagListUiState.copy(
                    tags = taskRepository.getTags().map { TaskMarkUiElement(it) })
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update { tagListUiState ->
                tagListUiState.copy(
                    tags = taskRepository.getProjects().map { TaskMarkUiElement(it) })
            }
        }
    }

    companion object {

        fun provideFactory(
            isTag: Boolean,
            taskRepository: TaskRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskMarkerModel(isTag, taskRepository) as T
                }
            }

    }


}


data class TagListUiState(val tags: List<TaskMarkUiElement> = emptyList())