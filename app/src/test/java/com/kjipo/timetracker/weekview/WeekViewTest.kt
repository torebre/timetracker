package com.kjipo.timetracker.weekview

import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDate

class WeekViewTest {


    @Test
    fun `Test that day list generation is correct if today is Sunday`() {
        val testDate = LocalDate.of(2023, 12, 17)
        val days = WeekViewModel.generateDaysFromStartOfWeekUntilToday(testDate)
        val expectedDays = (0 until 7).map { testDate.minusDays(it.toLong()) }.reversed()

        assertEquals(expectedDays, days)
    }

    @Test
    fun `Test that day list generation is correct if today is some other day than Sunday`() {
        val testDate = LocalDate.of(2023, 12, 14)
        val days = WeekViewModel.generateDaysFromStartOfWeekUntilToday(testDate)
        val expectedDays = (0 until 4).map { testDate.minusDays(it.toLong()) }.reversed()

        assertEquals(expectedDays, days)
    }


}