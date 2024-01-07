package com.kjipo.timetracker.weekview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskWithTimeEntries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId


class WeekViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(WeekViewState(emptyList()))

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.update {
                it.copy(daySummaries = generateTaskList())
            }
        }
    }

    private suspend fun generateTaskList(): List<DaySummary> {
        return generateDaysFromStartOfWeekUntilToday()
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
                    val durationTimeEntries = if (tasksWithTimeEntries.timeEntries.isEmpty()) {
                        Duration.ZERO
                    } else {
                        tasksWithTimeEntries.getTimeEntriesCompletelyWithinInterval(localDate.atStartOfDay()
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

                    // These are entries with no specific start or stop time.
                    // They consist of a day and a duration
                    val durationDayEntries =
                        tasksWithTimeEntries.getTimeDayEntriesForDate(localDate)
                            .let { timeEntriesToInclude ->
                                if (timeEntriesToInclude.isEmpty()) {
                                    Duration.ZERO
                                } else {
                                    timeEntriesToInclude.map {
                                        it.duration
                                    }.reduce { duration1, duration2 ->
                                        duration1.plus(duration2)
                                    }
                                }
                            }

                    durationTimeEntries.plus(durationDayEntries)
                }.reduce { duration1, duration2 ->
                    duration1.plus(duration2)
                }
                DayTaskSummary(entry.key.title, totalDuration)
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


data class DayTaskSummary(val title: String, val duration: Duration)

data class DaySummary(
    val date: LocalDate,
    val timeLogged: Duration,
    val tasks: List<DayTaskSummary>
)

data class WeekViewState(val daySummaries: List<DaySummary>)