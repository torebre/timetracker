package com.kjipo.timetracker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjipo.timetracker.database.*
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TaskTagStorageTest {
    private lateinit var timeEntryDao: TimeEntryDao
    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        timeEntryDao = database.timeEntryDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun storeTaskTagConnectionTest() {
        val tag = Tag(title = "Test tag")
        database.tagDao().insertTag(tag).also { tag.tagId = it }

        val task = Task(0, "Test task")
        val taskId = database.taskDao().insertTask(task)

        database.taskDao().insertTaskAndTagCrossRef(TagTasksCrossRef(taskId, tag.tagId))

        val taskWithTimeEntries = database.taskDao().getTaskWithTimeEntries(taskId)

        ViewMatchers.assertThat(taskWithTimeEntries?.tags, CoreMatchers.equalTo(listOf(tag)))
    }


}