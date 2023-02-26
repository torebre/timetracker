package com.kjipo.timetracker.tagscreen


import androidx.compose.ui.graphics.Color
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
import timber.log.Timber


class TagScreenModel(private val taskRepository: TaskRepository) : ViewModel() {

    private var currentTag: Tag? = null

    private val viewModelState = MutableStateFlow(TagScreenUiState(TagUi(0, "", null), true))

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    fun setCurrentTag(id: Long) {

        Timber.tag("TagScreen").i("Setting current tag: $id")

        viewModelState.update { it.copy(loading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            currentTag = taskRepository.getTag(id)
        }

        Timber.tag("TagScreen").i("Current tag: ${currentTag}")

        currentTag?.let {
            viewModelState.update { tagListUiState ->
                tagListUiState.copy(
                    tag = TagUi(it),
                    loading = false
                )
            }
        }
    }

    fun updateTag(tagUi: TagUi) {
        val tag = currentTag
        viewModelScope.launch(Dispatchers.IO) {
            currentTag =
                tag?.copy(title = tagUi.title, colour = tagUi.colour?.toAndroidGraphicsColor())
                    ?: taskRepository.insertTag(
                        Tag(
                            0,
                            tagUi.title,
                            tagUi.colour?.toAndroidGraphicsColor()
                        )
                    ).let { newTagId ->
                        taskRepository.getTag(newTagId)
                    }
        }
    }

    fun deleteTag() {
        viewModelScope.launch(Dispatchers.IO) {
            currentTag?.apply {
                taskRepository.deleteTag(this)
            }
            currentTag = null
        }

    }


    companion object {

        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TagScreenModel(taskRepository) as T
                }
            }

    }


}


fun Color.toAndroidGraphicsColor() = android.graphics.Color.valueOf(red, green, blue, alpha)


data class TagScreenUiState(val tag: TagUi, val loading: Boolean = false)
