package com.kjipo.timetracker.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TimeEntry::class, Task::class, Tag::class,
        TimeEntryTaskCrossRef::class, TagTasksCrossRef::class,
        TimeEntryDay::class, Project::class],
    version = 8,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 7, to = 8)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tagDao(): TagDao

    abstract fun taskDao(): TaskDao

    abstract fun timeEntryDao(): TimeEntryDao

    abstract fun projectDao(): ProjectDao

}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Task ADD COLUMN lastUpdated INTEGER")
    }
}
