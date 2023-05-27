package org.catrobat.paintroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.catrobat.paintroid.adapter.ProjectAdapter
import org.catrobat.paintroid.data.local.database.ProjectDatabase
import org.catrobat.paintroid.data.local.database.ProjectDatabaseProvider
import org.catrobat.paintroid.model.Project

lateinit var projectDB: ProjectDatabase
class LandingPage: AppCompatActivity() {
    private lateinit var projectsRecyclerView: RecyclerView
    private lateinit var projectsList: ArrayList<Project>
    private lateinit var projectAdapter: ProjectAdapter

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

        init()

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

        val landingPageBtnLoadImage = findViewById<FloatingActionButton>(R.id.fab_pocket_paint_load_project)
        landingPageBtnLoadImage.setOnClickListener{
            Toast.makeText(this, "Load Image", Toast.LENGTH_SHORT).show()
            val loadImageIntent = Intent(this, MainActivity::class.java)
            loadImageIntent.putExtra("LOAD_IMAGE", "load_image")
            startActivity(loadImageIntent)
        }

        val previewImage = findViewById<ImageView>(R.id.iv_pocket_paint_image_preview)
        previewImage.setOnClickListener {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
    }

    private fun init() {
        projectsRecyclerView = findViewById(R.id.rv_pocket_paint_projects_list)
        projectsRecyclerView.layoutManager = LinearLayoutManager(this)

        projectsList = ArrayList()
        projectDB.dao.getProjects().forEach {
            projectsList.add(it)
        }

        projectAdapter = ProjectAdapter(projectsList)
        projectsRecyclerView.adapter = projectAdapter
    }
}