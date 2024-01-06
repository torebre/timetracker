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
            .map {
                DaySummary(
                    it,
                    generateDaySummary(
                        taskRepository.getTasksWithTimeEntries(
                            it.atStartOfDay(),
                            it.atTime(23, 59, 59)
                        )
                    )
                )
            }


    }

    private fun generateDaySummary(tasksForDay: List<TaskWithTimeEntries>): List<DayTaskSummary> {
        return tasksForDay.groupBy { it.task }
            .map { entry ->
                val totalDuration = entry.value.map { tasksWithTimeEntries ->
                    val durationTimeEntries = tasksWithTimeEntries.timeEntries.map { timeEntry ->
                        timeEntry.getDurationMissingStopSetToNow()
                    }.reduce { duration1, duration2 ->
                        duration1.plus(duration2)
                    }

                    // These are entries with no specific start or stop time.
                    // They consist of a day and a duration
                    val durationDayEntries = tasksWithTimeEntries.timeEntriesDay.map {
                        it.duration
                    }.reduce { duration1, duration2 ->
                        duration1.plus(duration2)
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

data class DaySummary(val date: LocalDate, val tasks: List<DayTaskSummary>)

data class WeekViewState(val daySummaries: List<DaySummary>)