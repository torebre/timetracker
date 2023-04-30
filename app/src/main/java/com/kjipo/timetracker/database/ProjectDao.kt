package com.kjipo.timetracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProjectDao {

    @Insert
    fun insertProject(project: Project): Long

    @Update
    fun updateProject(project: Project)

    @Delete
    fun deleteProject(project: Project)

    @Query("SELECT * FROM project")
    fun getProjects(): List<Project>

    @Query("SELECT * FROM project WHERE project.projectId = :id")
    fun getProject(id: Long): Project?

}