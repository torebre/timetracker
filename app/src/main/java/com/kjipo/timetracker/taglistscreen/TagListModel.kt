package com.kjipo.timetracker.taglistscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagModel(private val isTag: Boolean, private val taskRepository: TaskRepository) : ViewModel() {
    private val viewModelState = MutableStateFlow(TagListUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    init {
        if(isTag) {
            loadTags()
        }
        else {
            loadProjects()
        }
    }


    fun insertTag(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if(isTag) {
                taskRepository.insertTag(Tag(0, title))
            }
            else {
                taskRepository.insertProject(Project(0, title))
            }
        }
        loadTags()
    }


    fun updateTag(id: Long, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if(isTag) {
                taskRepository.updateTag(Tag(id, title))
            }
            else {
                taskRepository.updateProject(Project(id, title))
            }
        }

        if(isTag) {
            loadTags()
        }
        else {
            loadProjects()
        }
    }


    fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update { tagListUiState ->
                tagListUiState.copy(
                    tags = taskRepository.getTags().map { TaskMarkUiElement(it) })
            }
        }
    }

    fun loadProjects() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update { tagListUiState ->
                tagListUiState.copy(
                    tags = taskRepository.getProjects().map { TaskMarkUiElement(it) })
            }
        }
    }

    companion object {

        fun provideFactory(isTag: Boolean, taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TagModel(isTag, taskRepository) as T
                }
            }

    }


}


data class TagListUiState(val tags: List<TaskMarkUiElement> = emptyList())