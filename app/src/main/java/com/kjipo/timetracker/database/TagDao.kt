package com.kjipo.timetracker.database

import androidx.room.*

@Dao
interface TagDao {

    @Insert
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tag")
    suspend fun getTags(): List<Tag>

    @Transaction
    @Query("SELECT * FROM tag WHERE tag.tagId = :tagId")
    suspend fun getTasksForTag(tagId: Long): List<TagWithTaskEntries>

    @Query("SELECT * FROM tag WHERE tag.tagId = :id")
    suspend fun getTag(id: Long): Tag?

}