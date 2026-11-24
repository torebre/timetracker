package com.kjipo.timetracker.weekoverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

class WeekOverviewViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private var selectedDate = Instant.now().atZone(ZoneId.systemDefault())

    private val viewModelState = MutableStateFlow(
        buildInitialState(selectedDate)
    )

    val uiState: StateFlow<WeekOverviewState> = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadWeek(selectedDate)
        }
    }

    private fun buildInitialState(date: ZonedDateTime): WeekOverviewState {
        val weekFields = WeekFields.of(Locale.getDefault())
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate()
        val sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate()
        return WeekOverviewState(
            weekNumber = date.get(weekFields.weekOfWeekBasedYear()),
            startDate = monday,
            endDate = sunday,
            days = emptyList()
        )
    }

    private suspend fun loadWeek(date: ZonedDateTime) {
        val weekFields = WeekFields.of(Locale.getDefault())
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate()
        val sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate()
        val weekNumber = date.get(weekFields.weekOfWeekBasedYear())

        val days = (0 until 7).map { offset ->
            val day = monday.plusDays(offset.toLong())
            val tasksForDay = taskRepository.getTasksWithTimeEntries(
                day.atStartOfDay(),
                day.atTime(23, 59, 59)
            )
            OverviewDaySummary(
                date = day,
                segments = generateSegments(tasksForDay, day)
            )
        }

        viewModelState.update {
            it.copy(
                weekNumber = weekNumber,
                startDate = monday,
                endDate = sunday,
                days = days
            )
        }
    }

    private fun generateSegments(
        tasksForDay: List<TaskWithTimeEntries>,
        localDate: LocalDate
    ): List<DayTimeSegment> {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = localDate.atStartOfDay(zoneId).toInstant()
        val endOfDay = localDate.plusDays(1).atStartOfDay(zoneId).toInstant()

        return tasksForDay.flatMap { taskWithEntries ->
            taskWithEntries.timeEntries.mapNotNull { entry ->
                val entryEnd = entry.stop
                    ?: if (!localDate.isAfter(LocalDate.now())) Instant.now() else return@mapNotNull null

                if (entry.start.isAfter(endOfDay) || entryEnd.isBefore(startOfDay)) return@mapNotNull null

                val clampedStart = if (entry.start.isBefore(startOfDay)) startOfDay else entry.start
                val clampedEnd = if (entryEnd.isAfter(endOfDay)) endOfDay else entryEnd

                val startMinutes = Duration.between(startOfDay, clampedStart).toMinutes().toInt()
                val endMinutes = Duration.between(startOfDay, clampedEnd).toMinutes().toInt()

                if (startMinutes >= endMinutes) return@mapNotNull null
                DayTimeSegment(startMinutes, endMinutes, taskWithEntries.tags)
            }
        }.sortedBy { it.startMinutes }
    }

    fun goToPreviousWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedDate = selectedDate.minusWeeks(1)
            loadWeek(selectedDate)
        }
    }

    fun goToNextWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedDate = selectedDate.plusWeeks(1)
            loadWeek(selectedDate)
        }
    }

    companion object {
        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WeekOverviewViewModel(taskRepository) as T
                }
            }
    }
}

data class DayTimeSegment(
    val startMinutes: Int,
    val endMinutes: Int,
    val tags: List<Tag>
)

data class OverviewDaySummary(
    val date: LocalDate,
    val segments: List<DayTimeSegment>
)

data class WeekOverviewState(
    val weekNumber: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<OverviewDaySummary>
)
