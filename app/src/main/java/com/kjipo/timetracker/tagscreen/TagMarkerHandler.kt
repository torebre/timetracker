package com.kjipo.timetracker.tagscreen

import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

internal class TagMarkerHandler(
    private val taskRepository: TaskRepository,
    private val viewModelScope: CoroutineScope,
    override val viewModelState: MutableStateFlow<TaskMarkElementUiState> = MutableStateFlow(
        TaskMarkElementUiState(TaskMarkUiElement(0, "", null), true)
    )
) : TaskMarkerHandler {

    private var currentTag: Tag? = null


    override fun setCurrentTag(id: Long) {
        Timber.tag("TagScreen").i("Setting current tag: $id")

        if (id == 0L) {
            // This means a new tag should be created
            viewModelState.update { it.copy(loading = false) }
            return
        }

        viewModelState.update { it.copy(loading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            currentTag = taskRepository.getTag(id)
        }

        currentTag?.let {
            viewModelState.update { tagListUiState ->
                tagListUiState.copy(
                    tag = TaskMarkUiElement(it),
                    loading = false
                )
            }
        }
    }

    override fun updateTag(tagUi: TaskMarkUiElement) {
        viewModelScope.launch(Dispatchers.IO) {
            val tag = currentTag
            currentTag = if (tag == null) {
                Tag(title = tagUi.title, colour = tagUi.colour?.toAndroidGraphicsColor()).apply {
                    taskRepository.insertTag(this).also { this.tagId = it }
                }
            } else {
                tag.copy(title = tagUi.title, colour = tagUi.colour?.toAndroidGraphicsColor())
                    .also { taskRepository.updateTag(it) }
            }
        }
    }

    override fun deleteTag() {
        viewModelScope.launch(Dispatchers.IO) {
            currentTag?.apply {
                taskRepository.deleteTag(this)
            }
            currentTag = null
        }

    }

}