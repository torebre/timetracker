package com.kjipo.timetracker.tasklist


data class TaskListUiState(
    val activeTasks: Collection<Long> = emptyList(),
    val tasks: List<TaskUi> = emptyList()
)
