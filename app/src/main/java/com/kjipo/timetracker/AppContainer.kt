package com.kjipo.timetracker

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.MIGRATION_7_8
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskRepositoryImpl


interface AppContainer {
    val appDatabase: AppDatabase

    val taskRepository: TaskRepository
}


class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    override val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-database")
//            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_7_8)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // We can't use the DAO here because the database is not yet fully initialized
                    db.execSQL("INSERT INTO DayType (title, workingHours) VALUES ('Public holiday', 0.0)")
                    db.execSQL("INSERT INTO DayType (title, workingHours) VALUES ('Half day', 3.75)")
                }

                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Ensure the default day types exist even if the database was created without them (e.g. after migration)
                    db.execSQL("INSERT OR IGNORE INTO DayType (title, workingHours) VALUES ('Public holiday', 0.0)")
                    db.execSQL("INSERT OR IGNORE INTO DayType (title, workingHours) VALUES ('Half day', 3.75)")
                }
            })
            .build()
    }


    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(appDatabase)
    }


}