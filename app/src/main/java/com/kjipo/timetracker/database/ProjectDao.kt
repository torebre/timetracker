package com.kjipo.timetracker.database

import androidx.room.*

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

}