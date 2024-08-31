package com.kjipo.timetracker.tagscreen

import kotlinx.coroutines.flow.MutableStateFlow


sealed interface TaskMarkerHandler {

    val viewModelState: MutableStateFlow<TaskMarkElementUiState>


    fun setCurrentTag(id: Long)


    fun updateTag(tagUi: TaskMarkUiElement)


    fun deleteTag()

}


