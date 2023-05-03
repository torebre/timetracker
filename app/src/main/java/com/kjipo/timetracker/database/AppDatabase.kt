package com.kjipo.timetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TimeEntry::class, Task::class, Tag::class,
        TimeEntryTaskCrossRef::class, TagTasksCrossRef::class,
        TimeEntryDay::class, Project::class], version = 6
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tagDao(): TagDao

    abstract fun taskDao(): TaskDao

    abstract fun timeEntryDao(): TimeEntryDao

    abstract fun projectDao(): ProjectDao

}