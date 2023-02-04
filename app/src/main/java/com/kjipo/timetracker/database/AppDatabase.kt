package com.kjipo.timetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TimeEntry::class, Task::class, Project::class,
    TimeEntryTaskCrossRef::class, ProjectTasksCrossRef::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    abstract fun taskDao(): TaskDao

    abstract fun timeEntryDao(): TimeEntryDao

}