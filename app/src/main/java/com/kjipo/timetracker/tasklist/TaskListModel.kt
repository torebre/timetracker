package com.kjipo.timetracker.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.taskscreen.TagUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
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
            sumTimeEntriesForTask(task),
            task.timeEntries.any { it.stop == null },
            tags = task.tags.map { TagUi(it) })
    }

    private fun sumTimeEntriesForTask(task: TaskWithTimeEntries): Duration {
        return task.timeEntries.sumOf { timeEntry ->
            val stop = timeEntry.stop ?: now()
            stop.toEpochMilli() - timeEntry.start.toEpochMilli()
        }
            .let { Duration.ofMillis(it) }
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
    val duration: Duration,
    val ongoing: Boolean,
    val tags: List<TagUi> = emptyList()
)