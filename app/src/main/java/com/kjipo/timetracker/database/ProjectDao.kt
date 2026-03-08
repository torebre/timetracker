package com.kjipo.timetracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProjectDao {

    @Insert
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("SELECT * FROM project")
    suspend fun getProjects(): List<Project>

    @Query("SELECT * FROM project WHERE project.projectId = :id")
    suspend fun getProject(id: Long): Project?

}