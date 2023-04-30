package com.kjipo.timetracker.tagscreen


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class TagScreenModel(isTagModel: Boolean, taskRepository: TaskRepository) : ViewModel() {
    private val taskMarkerHandler: TaskMarkerHandler

    init {
        taskMarkerHandler = if (isTagModel) {
            TagMarkerHandler(taskRepository, viewModelScope)
        } else {
            ProjectMarkerHandler(taskRepository, viewModelScope)
        }
    }

    val uiState = taskMarkerHandler.viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        taskMarkerHandler.viewModelState.value
    )


    fun setCurrentTag(id: Long) {
        taskMarkerHandler.setCurrentTag(id)
    }


    fun updateTag(tagUi: TaskMarkUiElement) {
        taskMarkerHandler.updateTag(tagUi)
    }


    fun deleteTag() {
        taskMarkerHandler.deleteTag()
    }


    companion object {

        fun provideFactory(
            isTagModel: Boolean,
            taskRepository: TaskRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TagScreenModel(isTagModel, taskRepository) as T
                }
            }

    }

}
