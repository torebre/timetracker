package com.kjipo.timetracker.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Sprint
import com.kjipo.timetracker.database.SprintDao
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import com.kjipo.timetracker.tasklist.TaskUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

data class SprintReportUiState(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val plannedDuration: Duration = Duration.ZERO,
    val unplannedDuration: Duration = Duration.ZERO,
    val totalAvailableDuration: Duration = Duration.ZERO,
    val tasks: List<TaskUi> = emptyList(),
    val availableSprints: List<Sprint> = emptyList(),
    val selectedSprintId: Long? = null
)

class SprintReportModel(
    private val taskRepository: TaskRepository,
    private val sprintDao: SprintDao
) : ViewModel() {

    private val defaultWorkingDuration = Duration.ofHours(7).plusMinutes(45)

    private val uiStateInternal = MutableStateFlow(
        SprintReportUiState(
            startDate = LocalDateTime.now().minusWeeks(2),
            endDate = LocalDateTime.now()
        )
    )
    val uiState: StateFlow<SprintReportUiState> = uiStateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            sprintDao.getAllSprints().collectLatest { sprints ->
                uiStateInternal.update { it.copy(availableSprints = sprints) }
                if (uiStateInternal.value.selectedSprintId == null && sprints.isNotEmpty()) {
                    selectSprint(sprints.first().sprintId)
                }
            }
        }
        updateReport()
    }

    fun selectSprint(sprintId: Long) {
        viewModelScope.launch {
            val sprint = sprintDao.getSprint(sprintId)
            if (sprint != null) {
                uiStateInternal.update {
                    it.copy(
                        selectedSprintId = sprintId,
                        startDate = sprint.startDate.atStartOfDay(),
                        endDate = sprint.endDate.atTime(LocalTime.MAX)
                    )
                }
                updateReport()
            }
        }
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
            val selectedSprintId = uiStateInternal.value.selectedSprintId

            val totalAvailableDuration = if (selectedSprintId != null) {
                calculateTotalAvailableDuration(selectedSprintId, start.toLocalDate(), end.toLocalDate())
            } else {
                Duration.ZERO
            }

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
                    totalAvailableDuration = totalAvailableDuration,
                    tasks = taskUis
                )
            }
        }
    }

    private suspend fun calculateTotalAvailableDuration(
        sprintId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Duration {
        val sprintDays = sprintDao.getSprintDays(sprintId).associateBy { it.date }
        val customDays = sprintDao.getCustomDays(sprintId).associateBy { it.date }
        val dayTypes = sprintDao.getDayTypes().associateBy { it.dayTypeId }

        var totalDuration = Duration.ZERO
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val customDay = customDays[currentDate]
            val sprintDay = sprintDays[currentDate]

            val dayDuration = when {
                customDay != null -> {
                    Duration.ofMinutes((customDay.workingHours * 60).toLong())
                }
                sprintDay != null -> {
                    val dayType = dayTypes[sprintDay.dayTypeId]
                    if (dayType != null) {
                        Duration.ofMinutes((dayType.workingHours * 60).toLong())
                    } else {
                        defaultWorkingDuration
                    }
                }
                else -> {
                    defaultWorkingDuration
                }
            }
            totalDuration = totalDuration.plus(dayDuration)
            currentDate = currentDate.plusDays(1)
        }

        return totalDuration
    }

    companion object {
        fun provideFactory(
            taskRepository: TaskRepository,
            sprintDao: SprintDao
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SprintReportModel(taskRepository, sprintDao) as T
                }
            }
    }
}
