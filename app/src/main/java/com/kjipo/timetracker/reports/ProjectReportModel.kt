package com.kjipo.timetracker.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class TaskReportData(
    val taskTitle: String,
    val totalDuration: Duration,
    val tags: List<String>
)

data class ProjectReportData(
    val projectId: Long?,
    val projectName: String,
    val totalDuration: Duration,
    val tasks: List<TaskReportData>
)

data class ProjectReportUiState(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val reportData: List<ProjectReportData> = emptyList(),
    val isGenerated: Boolean = false
) {
    val canGenerate: Boolean
        get() = startDate != null && endDate != null && !startDate.isAfter(endDate)
}

class ProjectReportModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(ProjectReportUiState())
    val uiState: StateFlow<ProjectReportUiState> = uiStateInternal.asStateFlow()

    fun setStartDate(date: LocalDate) {
        uiStateInternal.update { it.copy(startDate = date, isGenerated = false) }
    }

    fun setEndDate(date: LocalDate) {
        uiStateInternal.update { it.copy(endDate = date, isGenerated = false) }
    }

    fun generateReport() {
        val state = uiStateInternal.value
        val startDate = state.startDate ?: return
        val endDate = state.endDate ?: return
        if (startDate.isAfter(endDate)) return

        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()
            val rangeStart = startDate.atStartOfDay(zoneId).toInstant()
            val rangeEnd = endDate.plusDays(1).atStartOfDay(zoneId).toInstant()

            val allTasks = taskRepository.getTasksWithTimeEntries()
            val projectGroups = allTasks.groupBy { it.task.projectId }

            val reportData = mutableListOf<ProjectReportData>()

            projectGroups.forEach { (projectId, tasksInGroup) ->
                val taskReports = tasksInGroup.mapNotNull { taskWithEntries ->
                    val duration = computeTaskDurationInRange(
                        taskWithEntries, rangeStart, rangeEnd, startDate, endDate
                    )
                    if (duration > Duration.ZERO) {
                        TaskReportData(
                            taskTitle = taskWithEntries.task.title,
                            totalDuration = duration,
                            tags = taskWithEntries.tags.map { it.title }
                        )
                    } else null
                }

                if (taskReports.isNotEmpty()) {
                    val projectName = if (projectId != null) {
                        tasksInGroup.first().project?.title ?: "Unknown project"
                    } else {
                        "No project"
                    }
                    val totalDuration = taskReports.fold(Duration.ZERO) { acc, t -> acc + t.totalDuration }
                    reportData.add(
                        ProjectReportData(
                            projectId = projectId,
                            projectName = projectName,
                            totalDuration = totalDuration,
                            tasks = taskReports
                        )
                    )
                }
            }

            // Projects with IDs sorted by name first, "No project" at end
            val sorted = reportData.sortedWith(
                compareBy<ProjectReportData> { it.projectId == null }.thenBy { it.projectName }
            )

            uiStateInternal.update { it.copy(reportData = sorted, isGenerated = true) }
        }
    }

    private fun computeTaskDurationInRange(
        taskWithEntries: TaskWithTimeEntries,
        rangeStart: Instant,
        rangeEnd: Instant,
        startDate: LocalDate,
        endDate: LocalDate
    ): Duration {
        val timeEntryDuration = taskWithEntries.timeEntries.fold(Duration.ZERO) { acc, entry ->
            acc + computeEntryDurationInRange(entry, rangeStart, rangeEnd)
        }
        val timeEntryDayDuration = taskWithEntries.timeEntriesDay.fold(Duration.ZERO) { acc, day ->
            if (!day.date.isBefore(startDate) && !day.date.isAfter(endDate)) {
                acc + day.duration
            } else acc
        }
        return timeEntryDuration + timeEntryDayDuration
    }

    private fun computeEntryDurationInRange(
        entry: TimeEntry,
        rangeStart: Instant,
        rangeEnd: Instant
    ): Duration {
        val clampedStart = if (entry.start.isBefore(rangeStart)) rangeStart else entry.start
        val stop = entry.stop ?: Instant.now()
        val clampedEnd = if (stop.isAfter(rangeEnd)) rangeEnd else stop
        return if (clampedEnd.isAfter(clampedStart)) {
            Duration.between(clampedStart, clampedEnd)
        } else {
            Duration.ZERO
        }
    }

    companion object {
        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProjectReportModel(taskRepository) as T
                }
            }
    }
}
