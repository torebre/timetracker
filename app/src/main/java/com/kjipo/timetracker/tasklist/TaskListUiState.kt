package com.kjipo.timetracker.tasklist

import com.kjipo.timetracker.tagscreen.TaskMarkUiElement


data class TaskListUiState(
    val activeTasks: Collection<Long> = emptyList(),
    val tasks: List<TaskUi> = emptyList(),
    val availableFilters: List<TaskMarkUiElement> = emptyList(),
    val selectedFilters: List<TaskMarkUiElement> = emptyList()
)
