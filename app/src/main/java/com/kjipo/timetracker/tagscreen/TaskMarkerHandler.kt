package com.kjipo.timetracker.tagscreen

import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber


sealed interface TaskMarkerHandler {

    val viewModelState: MutableStateFlow<TaskMarkElementUiState>


    fun setCurrentTag(id: Long)


    fun updateTag(tagUi: TaskMarkUiElement)


    fun deleteTag()

}


class ProjectMarkerHandler(
    private val taskRepository: TaskRepository,
    private val viewModelScope: CoroutineScope,
    override val viewModelState: MutableStateFlow<TaskMarkElementUiState> = MutableStateFlow(
        TaskMarkElementUiState(TaskMarkUiElement(0, "", null), true)
    )
) : TaskMarkerHandler {

    private var currentProject: Project? = null


    override fun setCurrentTag(id: Long) {
        Timber.tag("TagScreen").i("Setting current tag: $id")

        if (id == 0L) {
            // This means a new tag should be created
            viewModelState.update { it.copy(loading = false) }
            return
        }

        viewModelState.update { it.copy(loading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            currentProject = taskRepository.getProject(id)
        }

        currentProject?.let {
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
            val project = currentProject
            currentProject = if (project == null) {
                Project(
                    title = tagUi.title,
                    colour = tagUi.colour?.toAndroidGraphicsColor()
                ).apply {
                    taskRepository.insertProject(this).also { this.projectId = it }
                }
            } else {
                project.copy(title = tagUi.title, colour = tagUi.colour?.toAndroidGraphicsColor())
                    .also { taskRepository.updateProject(it) }
            }
        }
    }

    override fun deleteTag() {
        viewModelScope.launch(Dispatchers.IO) {
            currentProject?.apply {
                taskRepository.deleteProject(this)
            }
            currentProject = null
        }

    }


}


class TagMarkerHandler(
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