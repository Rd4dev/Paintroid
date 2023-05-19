package org.catrobat.paintroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.catrobat.paintroid.data.local.database.ProjectDatabase
import org.catrobat.paintroid.data.local.database.ProjectDatabaseProvider
import org.catrobat.paintroid.model.Project

lateinit var projectDB: ProjectDatabase
class LandingPage: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pocketpaint_landing_page)

        /*val db by lazy {
            Room.databaseBuilder(applicationContext, ProjectDatabase::class.java, "projects.db")
                .allowMainThreadQueries()
                .build()
        }*/

        projectDB = ProjectDatabaseProvider.getDatabase(applicationContext)
//        projectDB.dao.insertProject(Project("singletonTestName", "paintroid/testPath1", "18/05/2023", "04/05/2023", "360X420", "jpeg", 70, "paintroid/imagePreviewTestPath1"))

//        db.dao.insertProject(Project("testName", "paintroid/testPath", "18/05/2023", "03/05/2023", "1920X1080", "png", 50, "paintroid/imagePreviewTestPath"))

        val landingPageToMainActivityBtn = findViewById<Button>(R.id.btn_pocket_paint_landing_page_btn)
        landingPageToMainActivityBtn.setOnClickListener {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }

        Log.d("databasedb", "onCreate: Database - ${projectDB.dao.getProjects()}")
        val landingPageBtnNewProject = findViewById<FloatingActionButton>(R.id.fab_pocket_paint_open_new_project)
        landingPageBtnNewProject.setOnClickListener {
            Toast.makeText(this, "New Clicked", Toast.LENGTH_SHORT).show()
            val newProjectIntent = Intent(this, MainActivity::class.java)
            newProjectIntent.putExtra("NEW_PROJECT", "new_project")
            startActivity(newProjectIntent)
        }
    }
}