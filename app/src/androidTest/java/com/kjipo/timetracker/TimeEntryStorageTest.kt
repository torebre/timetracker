package com.kjipo.timetracker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.ProjectDao
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.TimeEntryDao
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimeEntryStorageTest {
    private lateinit var timeEntryDao: TimeEntryDao
    private lateinit var projectDao: ProjectDao
    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        timeEntryDao = database.timeEntryDao()
        projectDao = database.projectDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun storeProjectTest() = runBlocking {
        val title = "Test project"
        val project = Project(title = title)

        projectDao.insertProject(project)

        val projects = projectDao.getProjects()

        assertThat(projects.size, equalTo(1))
    }

}