package com.kjipo.timetracker.database

import android.util.JsonReader
import android.util.JsonWriter
import androidx.compose.ui.graphics.Color
import androidx.room.TypeConverter
import java.io.StringReader
import java.io.StringWriter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromStringToLocalDate(value: String?): LocalDate? {
        return value?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    @TypeConverter
    fun localDateToString(localDate: LocalDate?): String? {
        return localDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun fromLongToDuration(value: Long?): Duration? {
        return value?.let {
            Duration.ofSeconds(it)
        }
    }

    @TypeConverter
    fun durationToLong(value: Duration?): Long? {
        return value?.let {
            // This is to support versions where the Duration.toSeconds() method does not exist
            it.toMillis() / 1000
        }
    }

    @TypeConverter
    fun fromLong(value: Long?): Color? {
        return value?.let { Color(it.toULong()) }
    }

    @TypeConverter
    fun colorToLong(value: Color?): Long? {
        return value?.value?.toLong()
    }


    @TypeConverter
    fun fromStringToColour(value: String?): android.graphics.Color? {
        return value?.let { colourString ->
            JsonReader(StringReader(colourString)).use { jsonReader ->
                jsonReader.beginArray()

                android.graphics.Color.valueOf(
                    jsonReader.nextDouble().toFloat(),
                    jsonReader.nextDouble().toFloat(),
                    jsonReader.nextDouble().toFloat()
                )
            }
        }
    }


    @TypeConverter
    fun fromColourToString(value: android.graphics.Color?): String? {
        return value?.let { colour ->
            val writer = StringWriter()
            JsonWriter(writer).use { jsonWriter ->
                jsonWriter.beginArray()
                    .value(colour.red())
                    .value(colour.green())
                    .value(colour.blue())
                    .endArray()
            }
            writer.toString()
        }

    }

}