package org.catrobat.paintroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.catrobat.paintroid.adapter.ProjectAdapter
import org.catrobat.paintroid.data.local.database.ProjectDatabase
import org.catrobat.paintroid.data.local.database.ProjectDatabaseProvider
import org.catrobat.paintroid.model.Project
import org.catrobat.paintroid.common.ABOUT_DIALOG_FRAGMENT_TAG
import org.catrobat.paintroid.common.MainActivityConstants
import org.catrobat.paintroid.common.REQUEST_CODE_INTRO
import org.catrobat.paintroid.dialog.AboutDialog

lateinit var projectDB: ProjectDatabase
class LandingPage: AppCompatActivity() {
    private lateinit var projectsRecyclerView: RecyclerView
    private lateinit var projectsList: ArrayList<Project>
    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pocketpaint_landing_page)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.pocketpaint_landingPage_toolbar)
        setSupportActionBar(toolbar)

        /*val db by lazy {
            Room.databaseBuilder(applicationContext, ProjectDatabase::class.java, "projects.db")
                .allowMainThreadQueries()
                .build()
        }*/

        projectDB = ProjectDatabaseProvider.getDatabase(applicationContext)
//        projectDB.dao.insertProject(Project("singletonTestName", "paintroid/testPath1", "18/05/2023", "04/05/2023", "360X420", "jpeg", 70, "paintroid/imagePreviewTestPath1"))

//        db.dao.insertProject(Project("testName", "paintroid/testPath", "18/05/2023", "03/05/2023", "1920X1080", "png", 50, "paintroid/imagePreviewTestPath"))

        //delete all projects
        //projectDB.dao.deleteAllProjects()

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

        /*//trigger on click of recyclerview item
        //pass multiple putExtra to pass Uri as one parameter
        //interactor.loadImage()
        val loadProjectIntent = Intent(this, MainActivity::class.java)
        loadProjectIntent.putExtra("LOAD_PROJECT", "load_project")
        startActivity(loadProjectIntent)*/

        val previewImage = findViewById<ImageView>(R.id.iv_pocket_paint_image_preview)
        previewImage.setOnClickListener {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pocketpaint_main_options, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pocketpaint_options_rate_us -> openPlayStore(applicationContext.packageName)
            R.id.pocketpaint_options_help -> startWelcomeActivity(REQUEST_CODE_INTRO)
            R.id.pocketpaint_options_about -> showAboutDialog()
            R.id.pocketpaint_options_feedback -> sendFeedback()
            /*R.id.pocketpaint_options_help -> presenterMain.showHelpClicked()
            R.id.pocketpaint_options_about -> presenterMain.showAboutClicked()
            R.id.pocketpaint_options_feedback -> presenterMain.sendFeedback()*/
            else -> return false
        }
        return true
    }

    @SuppressWarnings("SwallowedException")
    private fun openPlayStore(applicationId: String) {
        val uriPlayStore = Uri.parse("market://details?id=$applicationId")
        val openPlayStore = Intent(Intent.ACTION_VIEW, uriPlayStore)
        try {
            startActivity(openPlayStore)
        } catch (e: ActivityNotFoundException) {
            val uriNoPlayStore = Uri.parse("http://play.google.com/store/apps/details?id=$applicationId")
            val noPlayStoreInstalled = Intent(Intent.ACTION_VIEW, uriNoPlayStore)

            runCatching {
                startActivity(noPlayStoreInstalled)
            }
        }
    }

    private fun startWelcomeActivity(@MainActivityConstants.ActivityRequestCode requestCode: Int) {
        val intent = Intent(applicationContext, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, requestCode)
    }

    private fun showAboutDialog() {
        val about = AboutDialog()
        about.show(this.supportFragmentManager, ABOUT_DIALOG_FRAGMENT_TAG)
    }

    private fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO)
        val data = Uri.parse("mailto:support-paintroid@catrobat.org")
        intent.data = data
        startActivity(intent)
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

        projectAdapter.setOnItemClickListener(object: ProjectAdapter.OnItemClickListener{
            override fun onItemClick(position: Int, projectUri: String) {
                Log.d("clicklistener", "onItemClick: here with position - $position, project uri - $projectUri")
                val loadProjectIntent = Intent(applicationContext, MainActivity::class.java)
                loadProjectIntent.putExtra("LOAD_PROJECT", "load_project")
                loadProjectIntent.putExtra("PROJECT_URI", projectUri)
                startActivity(loadProjectIntent)
            }
        })
    }

    fun getVersionCode(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName
    }.getOrDefault("")
}