package com.kjipo.timetracker.reports

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.database.TimeEntryDay
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

    init {
        setSelectedTimeRange(SelectedTimeRange.DAY)
        loadTags()
    }


    fun setSelectedTimeRange(timeRange: SelectedTimeRange) {
        viewModelScope.launch(Dispatchers.IO) {
            val startAndStopTime = when (timeRange) {
                SelectedTimeRange.DAY -> {
                    getDayRange()
                }

                SelectedTimeRange.WEEK -> {
                    getWeekRange()
                }

                SelectedTimeRange.CUSTOM -> {
                    viewModelState.value.customRange
                }
            }

            updateTimeSummaries(startAndStopTime, timeRange)
        }

    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.value = viewModelState.value.copy(tags = taskRepository.getTags())
        }
    }

    private suspend fun updateTimeSummaries() {
        updateTimeSummaries(
            viewModelState.value.customRange,
            viewModelState.value.selectedTimeRange
        )
    }

    private suspend fun updateTimeSummaries(
        startAndStopTime: DateRange,
        timeRange: SelectedTimeRange
    ) {
        // TODO Need to get individual time entries and add them together, the task can have entries that should not be part of the summary
        val timeEntries = taskRepository.getTasksWithTimeEntries(
            startAndStopTime.startTime,
            startAndStopTime.stopTime
        )

        Timber.tag("Report").d("Number of time entries: ${timeEntries.size}")

        viewModelState.update {
            it.copy(
                selectedTimeRange = timeRange,
                projectSummaries = transformTimeEntriesToProjectSummaries(timeEntries),
                taskSummaries = transformEntriesToTaskSummaries(timeEntries, startAndStopTime)
            )
        }
    }

    private fun transformEntriesToTaskSummaries(
        tasksWithTimeEntries: List<TaskWithTimeEntries>,
        dateRange: DateRange
    ): List<TaskSummary> {
        val startInstant =
            dateRange.startTime.toInstant(ZoneId.systemDefault().rules.getOffset(dateRange.startTime))
        val stopInstant =
            dateRange.stopTime.toInstant(ZoneId.systemDefault().rules.getOffset(dateRange.stopTime))

        return tasksWithTimeEntries.map { taskWithTimeEntry ->
            val totalTimeForUsedForTaskInDateRange =
                taskWithTimeEntry.timeEntries.mapNotNull { timeEntry ->
                    val timeEntryStop = timeEntry.stop

                    if (timeEntry.start.isAfter(stopInstant)) {
                        null
                    } else if (timeEntryStop != null && timeEntryStop.isBefore(startInstant)) {
                        null
                    } else {
                        val start = if (timeEntry.start.isBefore(startInstant)) {
                            startInstant
                        } else {
                            timeEntry.start
                        }

                        val stop =
                            if (timeEntryStop == null || timeEntryStop.isAfter(stopInstant)) {
                                stopInstant
                            } else {
                                timeEntryStop
                            }

                        Duration.between(start, stop)
                    }
                }
                    .sumOf { it.seconds }

            TaskSummary(
                taskWithTimeEntry.task.title,
                Duration.ofSeconds(totalTimeForUsedForTaskInDateRange),
                taskWithTimeEntry.project,
                taskWithTimeEntry.tags)
        }.toList()
    }

    private suspend fun transformTimeEntriesToProjectSummaries(tasksWithTimeEntries: List<TaskWithTimeEntries>): List<ProjectSummary> {
        val projectIdDurationMap = mutableMapOf<Long, Duration>()

        for (tasksWithTimeEntry in tasksWithTimeEntries) {
            for (timeEntry in tasksWithTimeEntry.timeEntries) {
                addDurationsFromTimeEntry(
                    tasksWithTimeEntry,
                    timeEntry,
                    projectIdDurationMap,
                )
            }

            for (timeEntryDay in tasksWithTimeEntry.timeEntriesDay) {
                addDurationsFromTimeEntryDay(tasksWithTimeEntry, timeEntryDay, projectIdDurationMap)
            }
        }

        if (projectIdDurationMap.isEmpty()) {
            return emptyList()
        }

        val totalDurationInMilliseconds = projectIdDurationMap.values.reduce { value, next ->
            value.plus(next)
        }.toMillis()

        val projectSummaries = projectIdDurationMap.map { entry ->
            ProjectSummary(
                entry.key,
                "",
                entry.value,
                entry.value.toMillis().toDouble().div(totalDurationInMilliseconds)
            )
        }

        return addTitlesToProjectSummaries(projectSummaries)
    }

    private suspend fun addTitlesToProjectSummaries(projectDurations: List<ProjectSummary>): List<ProjectSummary> {
        val projectIdProjectMap = taskRepository.getProjects().map {
            Pair(it.projectId, it)
        }.toMap()

        return projectDurations.map { projectSummary ->
            if (projectSummary.projectId == noProjectId) {
                projectSummary.copy(title = "No project")
            } else {
                projectIdProjectMap[projectSummary.projectId]?.let {
                    projectSummary.copy(title = it.title)

                }
            }
        }.filterNotNull()
    }

    private fun addDurationsFromTimeEntryDay(
        task: TaskWithTimeEntries,
        timeEntryDay: TimeEntryDay,
        projectIdDurationMap: MutableMap<Long, Duration>
    ) {
        if (task.task.projectId == null) {
            addDurationToProject(
                noProjectId,
                timeEntryDay.duration,
                projectIdDurationMap
            )
        } else {
            addDurationToProject(
                task.task.projectId,
                timeEntryDay.duration,
                projectIdDurationMap
            )
        }
    }

    private fun addDurationsFromTimeEntry(
        task: TaskWithTimeEntries, timeEntry: TimeEntry,
        projectIdDurationMap: MutableMap<Long, Duration>
    ) {

        Timber.tag("Report")
            .d("Task ID: ${task.task.taskId}. Project ID: ${task.task.projectId}. Duration: ${timeEntry.getDurationMissingStopSetToNow()}")

        if (task.task.projectId == null) {
            addDurationToProject(
                noProjectId,
                timeEntry.getDurationMissingStopSetToNow(),
                projectIdDurationMap
            )
        } else {
            addDurationToProject(
                task.task.projectId,
                timeEntry.getDurationMissingStopSetToNow(),
                projectIdDurationMap
            )
        }
    }

    private fun addDurationToProject(
        projectId: Long,
        duration: Duration,
        projectIdDurationMap: MutableMap<Long, Duration>
    ) {
        if (projectIdDurationMap.contains(projectId)) {
            projectIdDurationMap[projectId] =
                projectIdDurationMap[projectId]!!.plus(duration)
        } else {
            projectIdDurationMap[projectId] = duration
        }

    }

    fun setCustomDateRange(start: LocalDateTime, stop: LocalDateTime) {
        viewModelScope.launch(Dispatchers.IO) {

            viewModelState.update {
                viewModelState.value.copy(
                    customRange = DateRange(
                        start,
                        stop
                    )
                )
            }

            updateTimeSummaries()
        }
    }


    companion object {

        const val noProjectId = -1L

        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReportsModel(taskRepository) as T
                }
            }


        fun getDayRange(): DateRange {
            val today = LocalDate.now()
            return DateRange(today.atStartOfDay(), today.atTime(23, 59, 59))
        }

        fun getWeekRange(): DateRange {
            val today = LocalDate.now()
            val startOfWeek =
                LocalDate.now().minusDays(today.dayOfWeek.value - 1L)
            val endOfWeek = today.plusDays(DayOfWeek.SUNDAY.value - today.dayOfWeek.value.toLong())

            return DateRange(startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59))
        }

    }


}

data class PieChartEntry(val tagId: Long, val percentage: Int, val colour: Color)

data class PieChartData(val pieChartEntries: List<PieChartEntry>)

data class ReportsUiState(
    val selectedTimeRange: SelectedTimeRange = SelectedTimeRange.DAY,
    val pieChartData: PieChartData? = null,
    val projectSummaries: List<ProjectSummary> = emptyList(),
    val customRange: DateRange = DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now()),
    val taskSummaries: List<TaskSummary> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedTags: List<Tag> = emptyList()
)

data class ProjectSummary(
    val projectId: Long,
    val title: String,
    val duration: Duration,
    val percentage: Double
)

data class TaskSummary(
    val title: String,
    val duration: Duration,
    val project: Project?,
    val tags: List<Tag>
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