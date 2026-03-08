package com.kjipo.timetracker.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Task
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DayModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())
    // Remove tasks MutableStateFlow as we will rely on the Flow from repository
    // private val tasks = MutableStateFlow<List<TaskWithTimeEntries>>(emptyList())

    val uiState = combine(
        selectedDate,
        taskRepository.getAllTasksWithTimeEntriesFlow()
    ) { date, tasks ->
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        val tasksForDay = tasks.mapNotNull { task ->
            val entriesForDay = task.timeEntries.filter { entry ->
                // Check if entry overlaps with the day
                val entryStart = entry.start
                val entryEnd = entry.stop

                entryStart.isBefore(endOfDay) && (entryEnd == null || entryEnd.isAfter(startOfDay))
            }

            if (entriesForDay.isNotEmpty()) {
                task.copy(timeEntries = entriesForDay)
            } else {
                null
            }
        }
        DayUiState(date, tasksForDay, tasks.map { it.task })
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DayUiState(LocalDate.now(), emptyList(), emptyList())
    )

    fun addTimeEntry(taskId: Long, start: Instant, stop: Instant?) {
        viewModelScope.launch {
            taskRepository.addTimeEntry(TimeEntry(taskId = taskId, start = start, stop = stop))
        }
    }

    companion object {
        fun provideFactory(
            taskRepository: TaskRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DayModel(taskRepository) as T
                }
            }
    }
}

data class DayUiState(
    val date: LocalDate,
    val tasks: List<TaskWithTimeEntries>,
    val allTasks: List<Task>
)
