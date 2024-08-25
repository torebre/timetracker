package com.kjipo.timetracker.tasklist

import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import java.time.Duration
import java.time.Instant
import java.time.Instant.now


data class TaskUi(
    val id: Long,
    val title: String,
    val timeEntries: List<TimeEntry>,
    val totalDuration: Duration,
    val tags: List<TaskMarkUiElement> = emptyList(),
    val project: TaskMarkUiElement? = null,
    val lastUpdated: Instant? = null
) {

    val mostRecentStopTime: Instant? = timeEntries.maxOfOrNull { timeEntry ->
        // If there is no stop time, the task is still ongoing
        timeEntry.stop ?: now()
    }


    private fun getCurrentStart(): TimeEntry? {
        return timeEntries.find { it.stop == null }
    }

    fun isOngoing() = getCurrentStart() != null

    fun getCurrentDuration(): Duration? {
        return getCurrentStart()?.let {
            Duration.between(it.start, now())
        }
    }

    fun computeTotalDuration(): Duration {
        return timeEntries.computeTotalDuration()
    }

    fun computeDurationOfNotOpenEntries(): Duration {
        return timeEntries.mapNotNull { it.getDuration() }.sumOf { it.toMillis() }
            .let { Duration.ofMillis(it) }
    }

}


fun List<TimeEntry>.computeTotalDuration(): Duration {
    return sumOf { timeEntry ->
        val stop = timeEntry.stop ?: now()
        stop.toEpochMilli() - timeEntry.start.toEpochMilli()
    }
        .let { Duration.ofMillis(it) }

}
