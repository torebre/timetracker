package com.kjipo.timetracker

import java.time.Duration
import java.time.format.DateTimeFormatter


val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E d. M")
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("kk:mm:ss")


fun Duration.toHoursPartHelper(): Int {
    return (toHours() % 24).toInt()
}

fun Duration.toMinutesPartHelper(): Int {
    return (toMinutes() % 60).toInt()
}

fun Duration.toSecondsPartHelper(): Int {
    return (seconds % 60).toInt()
}

fun toTwoDigits(value: Int): String {
    return if (value < 10) {
        "0${value}"
    } else {
        "$value"
    }
}
