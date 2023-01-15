package com.kjipo.timetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TimeEntry::class, Project::class, TimeEntryProjectCrossRef::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timeEntryDao(): TimeEntryDao

    abstract fun projectDao(): ProjectDao

}