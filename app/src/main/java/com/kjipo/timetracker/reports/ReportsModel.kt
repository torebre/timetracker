package com.kjipo.timetracker.reports

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.*


class ReportsModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(ReportsUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    fun setSelectedTimeRange(timeRange: SelectedTimeRange) {
        viewModelScope.launch(Dispatchers.IO) {
            val startAndStopTime = when (timeRange) {
                SelectedTimeRange.DAY -> {
                    val today = LocalDate.now()
                    DateRange(today.atStartOfDay(), today.atTime(23, 59, 59))
                }

                SelectedTimeRange.WEEK -> {
                    val today = LocalDate.now()
                    val startOfWeek =
                        LocalDate.now().minusDays(today.dayOfWeek.value - 1L)
                    val endOfWeek = today.plusDays(DayOfWeek.SUNDAY.value - today.dayOfWeek.value.toLong())
                    DateRange(startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59))
                }

                SelectedTimeRange.CUSTOM -> {
                    viewModelState.value.customRange
                }

            }

            // TODO Need to get individual time entries and add them together, the task can have entries that should not be part of the summary
            val timeEntries = taskRepository.getTasksWithTimeEntries(
                startAndStopTime.startTime,
                startAndStopTime.stopTime
            )

            viewModelState.update {
                it.copy(
                    selectedTimeRange = timeRange,
                    projectSummaries = transformTimeEntriesToProjectSummaries(timeEntries)
                )
            }
        }

    }

    private suspend fun transformTimeEntriesToProjectSummaries(tasksWithTimeEntries: List<TaskWithTimeEntries>): List<ProjectSummary> {
        var noProjectDuration = Duration.ofSeconds(0)
        val projectIdDurationMap = mutableMapOf<Long, Duration>()
        val projectIdProjectMap = mutableMapOf<Long, Project>()

        for (tasksWithTimeEntry in tasksWithTimeEntries) {
            for (timeEntry in tasksWithTimeEntry.timeEntries) {
                taskRepository.getTask(timeEntry.taskId)?.let { task ->
                    if (task.projectId != null) {
                        val project = taskRepository.getProject(task.projectId)

                        if (project == null) {
                            // TODO This is an error if it happens
                            noProjectDuration =
                                noProjectDuration.plus(timeEntry.getDurationMissingStopSetToNow())
                        } else {
                            projectIdProjectMap.putIfAbsent(project.projectId, project)
                            if (projectIdDurationMap.contains(project.projectId)) {
                                projectIdDurationMap[project.projectId] =
                                    projectIdDurationMap[project.projectId]!!.plus(timeEntry.getDurationMissingStopSetToNow())
                            }
                        }
                    } else {
                        noProjectDuration =
                            noProjectDuration.plus(timeEntry.getDurationMissingStopSetToNow())
                    }
                }
            }

            for (timeEntryDay in tasksWithTimeEntry.timeEntriesDay) {
                taskRepository.getTask(timeEntryDay.taskId)?.let { task ->
                    if (task.projectId != null) {
                        val project = taskRepository.getProject(task.projectId)

                        if (project == null) {
                            noProjectDuration =
                                noProjectDuration.plus(timeEntryDay.duration)
                        } else {
                            if (projectIdDurationMap.contains(project.projectId)) {
                                projectIdDurationMap[project.projectId] =
                                    projectIdDurationMap[project.projectId]!!.plus(timeEntryDay.duration)
                            }
                        }
                    } else {
                        noProjectDuration =
                            noProjectDuration.plus(timeEntryDay.duration)
                    }
                }
            }
        }

        if (projectIdDurationMap.isEmpty()) {
            return emptyList()
        }

        val totalDurationInMilliseconds = projectIdDurationMap.values.reduce { value, next ->
            value.plus(next)
        }.toMillis() + noProjectDuration.toMillis()

        return projectIdDurationMap.map { entry ->
            projectIdProjectMap[entry.key]?.let { project ->
                ProjectSummary(
                    project.title,
                    entry.value,
                    entry.value.toMillis().toDouble().div(totalDurationInMilliseconds)
                )
            }
        }.filterNotNull()

    }


    companion object {

        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReportsModel(taskRepository) as T
                }
            }

    }

}

data class PieChartEntry(val tagId: Long, val percentage: Int, val colour: Color)

data class PieChartData(val pieChartEntries: List<PieChartEntry>)

data class ReportsUiState(
    val selectedTimeRange: SelectedTimeRange = SelectedTimeRange.DAY,
    val pieChartData: PieChartData? = null,
    val projectSummaries: List<ProjectSummary> = emptyList(),
    val customRange: DateRange = DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now())
)

data class ProjectSummary(
    val title: String, val duration: Duration,
    val percentage: Double
)

data class DateRange(
    val startTime: LocalDateTime,
    val stopTime: LocalDateTime
)

data class CalendarUiState(
    val year: Year = Year.now(),
    val month: Month = LocalDate.now().month,
    val startTime: LocalDateTime? = null,
    val stopTime: LocalDateTime? = null
)


enum class SelectedTimeRange {
    DAY,
    WEEK,
    CUSTOM
}