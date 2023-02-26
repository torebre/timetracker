package com.kjipo.timetracker.database

import androidx.room.*

@Dao
interface TagDao {

    @Insert
    fun insertTag(tag: Tag): Long

    @Update
    fun updateTag(tag: Tag)

    @Delete
    fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tag")
    fun getTags(): List<Tag>

    @Transaction
    @Query("SELECT * FROM tag WHERE tag.tagId = :tagId")
    fun getTasksForTag(tagId: Long): List<TagWithTaskEntries>

    @Query("SELECT * FROM tag WHERE tag.tagId = :id")
    fun getTag(id: Long): Tag?

}