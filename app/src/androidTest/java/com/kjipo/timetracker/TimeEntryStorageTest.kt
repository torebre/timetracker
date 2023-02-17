package com.kjipo.timetracker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.database.TagDao
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
    private lateinit var tagDao: TagDao
    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        timeEntryDao = database.timeEntryDao()
        tagDao = database.tagDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun storeProjectTest() = runBlocking {
        val title = "Test project"
        val tag = Tag(title = title)

        tagDao.insertTag(tag)

        val projects = tagDao.getTags()

        assertThat(projects.size, equalTo(1))
    }

}