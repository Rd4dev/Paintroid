package org.catrobat.paintroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import data.local.database.ProjectDatabase
import org.catrobat.paintroid.common.PAINTROID_PICTURE_PATH
import org.catrobat.paintroid.model.Project

class LandingPage: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pocketpaint_landing_page)

        val db by lazy {
            Room.databaseBuilder(applicationContext, ProjectDatabase::class.java, "projects.db")
                .allowMainThreadQueries()
                .build()
        }

        db.dao.insertProject(Project("testName", "paintroid/testPath", "18/05/2023", "03/05/2023", "1920X1080", "png", 50, "paintroid/imagePreviewTestPath"))

        Log.d("databasedb", "onCreate: Database - ${db.dao.getProjects()}")
        val landingPageBtnNewProject = findViewById<FloatingActionButton>(R.id.fab_pocket_paint_open_new_project)
        landingPageBtnNewProject.setOnClickListener {
            Toast.makeText(this, "New Clicked", Toast.LENGTH_SHORT).show()
            val newProjectIntent = Intent(this, MainActivity::class.java)
            newProjectIntent.putExtra("NEW_PROJECT", "new_project")
            startActivity(newProjectIntent)
        }
    }
}