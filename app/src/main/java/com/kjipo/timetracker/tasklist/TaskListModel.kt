package com.kjipo.timetracker.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.Instant.now

class TaskListModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(TaskListUiState())

    private var sortOrder = SortOrder.DEFAULT

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            reloadTasks()
        }

        viewModelScope.launch(Dispatchers.IO) {
            // TODO Figure if there are problems with multiple threads updating the state using this
            while (isActive) {
                delay(1000)
                viewModelState.update { it.copy(tasks = refreshOngoingTasks(it.tasks)) }
            }
        }
    }

    fun toggleStartStop(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.getTaskWithTimeEntries(taskId)?.let { task ->
                val anyEntriesStopped = task.timeEntries.map { timeEntry ->
                    if (timeEntry.stop == null) {
                        taskRepository.setStopForTimeEntry(timeEntry.timeEntryId, now())
                        true
                    } else {
                        false
                    }
                }.any {
                    it
                }

                if (!anyEntriesStopped) {
                    // No ongoing entries, create a new time entry to start the task
                    taskRepository.addTimeEntry(TimeEntry(0, taskId, now()))
                }

                reloadTasks()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            reloadTasks()
        }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        this.sortOrder = sortOrder
        viewModelScope.launch(Dispatchers.IO) {
            reloadTasks()
        }
    }


    private fun refreshOngoingTasks(tasks: List<TaskUi>): List<TaskUi> {
        return tasks.map {
            if (it.isOngoing()) {
                it.copy(totalDuration = it.computeTotalDuration())
            } else {
                it
            }
        }.toList()
    }

    private suspend fun reloadTasks() {
        val tasks = getSortFunction(taskRepository.getTasksWithTimeEntries()
            .map { taskWithTimeEntries -> transformTaskToUiTask(taskWithTimeEntries) })
        val activeTasks = tasks.filter { it.isOngoing() }.map { it.id }

        viewModelState.update {
            viewModelState.value.copy(
                activeTasks = activeTasks,
                tasks = tasks
            )
        }
    }

    private fun getSortFunction(iterable: Iterable<TaskUi>): List<TaskUi> {
        return when (sortOrder) {
            SortOrder.DEFAULT -> {
                iterable.sortedBy { taskUi ->
                    // If there are no time entries assume the task is new
                    // and should be near the top of the list
                    taskUi.mostRecentStopTime ?: now()
                }.sortedBy { taskUi ->
                    // The sort is stable so this second sort will only move tasks
                    // that are ongoing to the top of the list and leave the other
                    // elements in the order provided by the first sort
                    if (taskUi.isOngoing()) {
                        0
                    } else {
                        1
                    }
                }
            }

            SortOrder.RECENTLY_USED -> {
                iterable.sortedBy { taskUi ->
                    taskUi.lastUpdated?.toEpochMilli() ?: 0
                }

            }


        }

    }


    private fun transformTaskToUiTask(task: TaskWithTimeEntries): TaskUi {
        return TaskUi(
            task.task.taskId,
            task.task.title,
            task.timeEntries,
            task.timeEntries.computeTotalDuration(),
            tags = task.tags.map { TaskMarkUiElement(it) },
            project = task.project?.let {
                TaskMarkUiElement(it)
            },
            lastUpdated = task.task.lastUpdated
        )
    }

    companion object {

        fun provideFactory(
            taskRepository: TaskRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskListModel(taskRepository) as T
                }
            }
    }

}




