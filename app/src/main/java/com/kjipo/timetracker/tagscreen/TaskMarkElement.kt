package com.kjipo.timetracker.tagscreen

import androidx.compose.ui.graphics.Color
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.Tag





fun Color.toAndroidGraphicsColor() = android.graphics.Color.valueOf(red, green, blue, alpha)


data class TaskMarkElementUiState(val tag: TaskMarkUiElement, val loading: Boolean = false)


data class TaskMarkUiElement(val elementId: Long, val title: String, val colour: Color?) {

    constructor(tag: Tag) : this(tag.tagId, tag.title, tag.colour?.let {
        Color(it.red(), it.green(), it.blue())
    })

    constructor(project: Project) : this(project.projectId, project.title, project.colour?.let {
        Color(it.red(), it.green(), it.blue())
    })

    fun toTag() = Tag(tagId = elementId, title = title, colour = colour?.toAndroidGraphicsColor())

    fun toProject() =
        Project(projectId = elementId, title = title, colour = colour?.toAndroidGraphicsColor())

}
