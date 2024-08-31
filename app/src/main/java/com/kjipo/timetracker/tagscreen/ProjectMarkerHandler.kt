package com.kjipo.timetracker.tagscreen

import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber


internal class ProjectMarkerHandler(
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
                createNewProjectTag(tagUi)
            } else {
                project.copy(title = tagUi.title, colour = tagUi.colour?.toAndroidGraphicsColor())
                    .also { taskRepository.updateProject(it) }
            }
        }
    }

    private suspend fun createNewProjectTag(tagUi: TaskMarkUiElement): Project {
        return Project(
            title = tagUi.title,
            colour = tagUi.colour?.toAndroidGraphicsColor()
        ).apply {
            taskRepository.insertProject(this).also { this.projectId = it }
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