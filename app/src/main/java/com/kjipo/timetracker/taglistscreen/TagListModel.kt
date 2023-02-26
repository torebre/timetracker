package com.kjipo.timetracker.taglistscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.taskscreen.TagUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(TagListUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    init {
        loadTags()
    }


    fun insertTag(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateTag(Tag(0, title))
        }
        loadTags()
    }


    fun updateTag(id: Long, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateTag(Tag(id, title))
        }
        loadTags()
    }


    fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update { tagListUiState ->
                tagListUiState.copy(
                    tags = taskRepository.getTags().map { TagUi(it) })
            }
        }
    }


    companion object {

        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TagModel(taskRepository) as T
                }
            }

    }


}


data class TagListUiState(val tags: List<TagUi> = emptyList())