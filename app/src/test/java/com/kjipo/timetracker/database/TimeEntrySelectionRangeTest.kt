package com.kjipo.timetracker.database

import com.kjipo.timetracker.reports.ReportsModel
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

class TimeEntrySelectionRangeTest {


    @Test
    fun timeRangeDayIncludedWhenDateIsToday() {
        val timeEntryDay =
            TimeEntryDay(0, 1, LocalDate.now(), Duration.ofMinutes(10))

        val dayRange = ReportsModel.getDayRange()
        val shouldTimeEntryBeIncluded = TaskRepositoryImpl.shouldTimeEntryDayBeIncluded(
            timeEntryDay,
            dayRange.startTime,
            dayRange.stopTime
        )

        assertThat(shouldTimeEntryBeIncluded, equalTo(true))
    }

    @Test
    fun timeRangeDayIncludedWhenRangeIsWeekAndDateIsToday() {
        val timeEntryDay =
            TimeEntryDay(0, 1, LocalDate.now(), Duration.ofMinutes(10))

        val weekRange = ReportsModel.getWeekRange()
        val shouldTimeEntryBeIncluded = TaskRepositoryImpl.shouldTimeEntryDayBeIncluded(
            timeEntryDay,
            weekRange.startTime,
            weekRange.stopTime
        )

        assertThat(shouldTimeEntryBeIncluded, equalTo(true))
    }


    @Test
    fun timeEntryForTodayIncludedWhenRangeIsDay() {
        val startTime = LocalDate.now().atTime(12, 0, 0)
        val stopTime = LocalDate.now().atTime(14, 0, 0)

        val timeEntry =
            TimeEntry(
                0,
                1,
                startTime.toInstant(ZoneId.systemDefault().rules.getOffset(startTime)),
                stopTime.toInstant(ZoneId.systemDefault().rules.getOffset(stopTime))
            )

        val dayRange = ReportsModel.getDayRange()
        val shouldTimeEntryBeIncluded = TaskRepositoryImpl.shouldTimeEntryBeIncluded(
            timeEntry,
            dayRange.startTime,
            dayRange.stopTime
        )

        assertThat(shouldTimeEntryBeIncluded, equalTo(true))
    }

    @Test
    fun timeEntryForTodayIncludedWhenRangeIsWeek() {
        val startTime = LocalDate.now().atTime(12, 0, 0)
        val stopTime = LocalDate.now().atTime(14, 0, 0)

        val timeEntry =
            TimeEntry(
                0,
                1,
                startTime.toInstant(ZoneId.systemDefault().rules.getOffset(startTime)),
                stopTime.toInstant(ZoneId.systemDefault().rules.getOffset(stopTime))
            )

        val weekRange = ReportsModel.getWeekRange()
        val shouldTimeEntryBeIncluded = TaskRepositoryImpl.shouldTimeEntryBeIncluded(
            timeEntry,
            weekRange.startTime,
            weekRange.stopTime
        )

        assertThat(shouldTimeEntryBeIncluded, equalTo(true))
    }
}