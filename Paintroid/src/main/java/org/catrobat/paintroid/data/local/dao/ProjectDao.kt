package org.catrobat.paintroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.catrobat.paintroid.model.Project

@Dao
interface ProjectDao {

    @Insert
    fun insertProject(project: Project)

    @Query("SELECT * FROM Project ORDER BY lastModified DESC")
    fun getProjects(): List<Project>

    @Query("DELETE FROM Project WHERE id= :id")
    fun deleteProject(id: Int)
}