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
                viewModelState.update { it.copy(refreshOngoingTasks(it.tasks)) }
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
            }

            reloadTasks()
        }

    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            reloadTasks()
        }
    }


    private fun refreshOngoingTasks(tasks: List<TaskUi>): List<TaskUi> {
        return tasks.map {
            if (!it.isOngoing()) {
                it
            } else {
                it.copy(totalDuration = it.computeTotalDuration())
            }
        }.toList()
    }

    private suspend fun reloadTasks() {
        viewModelState.update {
            viewModelState.value.copy(
                tasks = taskRepository.getTasksWithTimeEntries()
                    .map { taskWithTimeEntries -> transformTaskToUiTask(taskWithTimeEntries) })
        }

    }


    private fun transformTaskToUiTask(task: TaskWithTimeEntries): TaskUi {
        return TaskUi(task.task.taskId,
            task.task.title,
            task.timeEntries,
            task.timeEntries.computeTotalDuration(),
            tags = task.tags.map { TaskMarkUiElement(it) })
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


data class TaskListUiState(val tasks: List<TaskUi> = emptyList())


data class TaskUi(
    val id: Long,
    val title: String,
    val timeEntries: List<TimeEntry>,
    val totalDuration: Duration,
    val tags: List<TaskMarkUiElement> = emptyList(),
    val project: TaskMarkUiElement? = null
) {

    fun getCurrentStart(): TimeEntry? {
        return timeEntries.find { it.stop == null }
    }

    fun isOngoing() = getCurrentStart() != null

    fun getCurrentDuration(): Duration? {
        return getCurrentStart()?.let {
            Duration.between(it.start, Instant.now())
        }
    }

    fun computeTotalDuration(): Duration {
        return timeEntries.computeTotalDuration()
    }
}

fun List<TimeEntry>.computeTotalDuration(): Duration {
    return sumOf { timeEntry ->
        val stop = timeEntry.stop ?: now()
        stop.toEpochMilli() - timeEntry.start.toEpochMilli()
    }
        .let { Duration.ofMillis(it) }

}
