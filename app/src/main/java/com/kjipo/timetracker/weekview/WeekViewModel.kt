package com.kjipo.timetracker.weekview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.Project
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


class WeekViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private var selectedDate = Instant.now().atZone(ZoneId.systemDefault())

    private val viewModelState = let {
        val weekData = getWeekData(selectedDate)

        MutableStateFlow(
            WeekViewState(
                weekData.dateToday,
                weekData.weekNumber,
                weekData.start,
                weekData.end,
                emptyList()
            )
        )
    }

    private fun getWeekData(date: ZonedDateTime): WeekData {
        val weekFields = WeekFields.of(Locale.getDefault())

        return WeekData(
            date.toLocalDate(),
            date.get(weekFields.weekOfWeekBasedYear()),
            date.with(TemporalAdjusters.previousOrSame(weekFields.firstDayOfWeek)).toLocalDate(),
            date.with(TemporalAdjusters.nextOrSame(DayOfWeek.entries[(weekFields.firstDayOfWeek.ordinal + 6) % DayOfWeek.values().size]))
                .toLocalDate()
        )
    }

    val uiState: StateFlow<WeekViewState> = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateWeekData(selectedDate)
        }
    }

    private suspend fun updateWeekData(dateToUse: ZonedDateTime) {
        viewModelState.update {
            val weekData = getWeekData(dateToUse)

            it.copy(
                weekData.dateToday,
                weekData.weekNumber,
                weekData.start,
                weekData.end,
                daySummaries = generateTaskList(weekData.dateToday)
            )
        }
    }

    private suspend fun generateTaskList(dateToday: LocalDate): List<DaySummary> {
        return generateDaysFromStartOfWeekUntilToday(dateToday)
            .map { localDate ->
                val daySummary = generateDaySummary(
                    localDate,
                    // The method getTaskWithTimeEntries does not filter out time entries for the
                    // task that are outside the interval, it returns the task with all time
                    // entries. The tasks returned will have one or more time entries within
                    // the interval given as input
                    taskRepository.getTasksWithTimeEntries(
                        localDate.atStartOfDay(),
                        localDate.atTime(23, 59, 59)
                    )
                )

                val timeLogged = if (daySummary.isEmpty()) {
                    Duration.ZERO
                } else {
                    daySummary.map { it.duration }.reduce { duration1, duration2 ->
                        duration1.plus(duration2)
                    }
                }

                DaySummary(
                    localDate,
                    timeLogged,
                    daySummary
                )
            }
    }

    private fun generateDaySummary(
        localDate: LocalDate,
        tasksForDay: List<TaskWithTimeEntries>
    ): List<DayTaskSummary> {
        return tasksForDay.groupBy { it.task }
            .map { entry ->
                val totalDuration = entry.value.map { tasksWithTimeEntries ->
                    val durationTimeEntries = computeDurationForDay(tasksWithTimeEntries, localDate)
                    // These are entries with no specific start or stop time.
                    // They consist of a day and a duration
                    val durationDayEntries = getDurationDayEntries(tasksWithTimeEntries, localDate)

                    durationTimeEntries.plus(durationDayEntries)
                }.reduce { duration1, duration2 ->
                    duration1.plus(duration2)
                }

                val firstEntry = entry.value.firstOrNull()
                DayTaskSummary(
                    entry.key.title, totalDuration,
                    firstEntry?.project,
                    firstEntry?.tags ?: emptyList()
                )
            }
    }

    private fun getDurationDayEntries(
        tasksWithTimeEntries: TaskWithTimeEntries,
        localDate: LocalDate
    ): Duration {
        tasksWithTimeEntries.getTimeDayEntriesForDate(localDate)
            .let { timeEntriesToInclude ->
                if (timeEntriesToInclude.isEmpty()) {
                    return Duration.ZERO
                } else {
                    return timeEntriesToInclude.map {
                        it.duration
                    }.reduce { duration1, duration2 ->
                        duration1.plus(duration2)
                    }
                }
            }
    }

    private fun computeDurationForDay(
        tasksWithTimeEntries: TaskWithTimeEntries,
        localDate: LocalDate
    ): Duration {
        if (tasksWithTimeEntries.timeEntries.isEmpty()) {
            Duration.ZERO
        }

        return tasksWithTimeEntries.getTimeEntriesCompletelyWithinInterval(
            localDate.atStartOfDay()
                .let {
                    it.toInstant(ZoneId.systemDefault().rules.getOffset(it))
                },
            localDate.atTime(23, 59, 59).let {
                it.toInstant(ZoneId.systemDefault().rules.getOffset(it))
            }).let { timeEntriesToInclude ->
            if (timeEntriesToInclude.isEmpty()) {
                Duration.ZERO
            } else {
                timeEntriesToInclude.map { timeEntry ->
                    timeEntry.getDurationMissingStopSetToNow()
                }.reduce { duration1, duration2 ->
                    duration1.plus(duration2)
                }
            }
        }
    }

    fun selectedWeekChanged(weekDiff: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update {
                selectedDate = selectedDate.plusWeeks(weekDiff)
                getWeekData(selectedDate).let { weekData ->
                    it.copy(
                        weekData.dateToday,
                        weekData.weekNumber,
                        weekData.start,
                        weekData.end,
                        generateTaskList(weekData.dateToday)
                    )
                }
            }
        }
    }

    companion object {

        internal fun generateDaysFromStartOfWeekUntilToday(day: LocalDate = LocalDate.now()): List<LocalDate> {
            return (0 until day.dayOfWeek.value).map {
                day.minusDays(it.toLong())
            }.reversed()
        }
    }

}


private class WeekData(
    val dateToday: LocalDate,
    val weekNumber: Int,
    val start: LocalDate,
    val end: LocalDate
)

data class DayTaskSummary(
    val title: String,
    val duration: Duration,
    val project: Project?,
    val tags: List<Tag>
)

data class DaySummary(
    val date: LocalDate,
    val timeLogged: Duration,
    val tasks: List<DayTaskSummary>
)

data class WeekViewState(
    val dateToday: LocalDate,
    val weekNumber: Int,
    val start: LocalDate,
    val end: LocalDate,
    val daySummaries: List<DaySummary>
)
