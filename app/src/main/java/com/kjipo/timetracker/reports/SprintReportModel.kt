package com.kjipo.timetracker.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import com.kjipo.timetracker.tasklist.TaskUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

data class SprintReportUiState(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val plannedDuration: Duration = Duration.ZERO,
    val unplannedDuration: Duration = Duration.ZERO,
    val tasks: List<TaskUi> = emptyList()
)

class SprintReportModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(
        SprintReportUiState(
            startDate = LocalDateTime.now().minusWeeks(2),
            endDate = LocalDateTime.now()
        )
    )
    val uiState: StateFlow<SprintReportUiState> = uiStateInternal.asStateFlow()

    init {
        updateReport()
    }

    fun setStartDate(date: LocalDateTime) {
        uiStateInternal.update { it.copy(startDate = date) }
        updateReport()
    }

    fun setEndDate(date: LocalDateTime) {
        uiStateInternal.update { it.copy(endDate = date) }
        updateReport()
    }

    private fun updateReport() {
        viewModelScope.launch {
            val start = uiStateInternal.value.startDate
            val end = uiStateInternal.value.endDate

            val tasksWithTimeEntries = taskRepository.getTasksWithTimeEntries(start, end)
            
            val taskUis = tasksWithTimeEntries.map { taskWithEntries ->
                TaskUi(
                    id = taskWithEntries.task.taskId,
                    title = taskWithEntries.task.title,
                    timeEntries = taskWithEntries.timeEntries.filter { 
                        it.start.isAfter(start.toInstant(ZoneOffset.UTC)) && 
                        (it.stop?.isBefore(end.toInstant(ZoneOffset.UTC)) ?: true)
                    },
                    totalDuration = Duration.ZERO, // Will be computed below
                    tags = taskWithEntries.tags.map { TaskMarkUiElement(it) },
                    project = taskWithEntries.project?.let { TaskMarkUiElement(it) },
                    closed = taskWithEntries.task.closed
                ).let { taskUi ->
                    taskUi.copy(totalDuration = taskUi.computeTotalDuration())
                }
            }.filter { it.totalDuration > Duration.ZERO }

            var plannedMillis = 0L
            var unplannedMillis = 0L

            taskUis.forEach { taskUi ->
                if (taskUi.tags.any { it.title.equals("Unplanned", ignoreCase = true) }) {
                    unplannedMillis += taskUi.totalDuration.toMillis()
                } else {
                    plannedMillis += taskUi.totalDuration.toMillis()
                }
            }

            uiStateInternal.update {
                it.copy(
                    plannedDuration = Duration.ofMillis(plannedMillis),
                    unplannedDuration = Duration.ofMillis(unplannedMillis),
                    tasks = taskUis
                )
            }
        }
    }

    companion object {
        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SprintReportModel(taskRepository) as T
                }
            }
    }
}
