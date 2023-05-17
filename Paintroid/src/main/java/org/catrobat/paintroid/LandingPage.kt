package org.catrobat.paintroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.catrobat.paintroid.common.PAINTROID_PICTURE_PATH

class LandingPage: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pocketpaint_landing_page)

        val landingPageBtnNewProject = findViewById<FloatingActionButton>(R.id.fab_pocket_paint_open_new_project)
        landingPageBtnNewProject.setOnClickListener {
            Toast.makeText(this, "New Clicked", Toast.LENGTH_SHORT).show()
            val newProjectIntent = Intent(this, MainActivity::class.java)
            newProjectIntent.putExtra("NEW_PROJECT", "new_project")
            startActivity(newProjectIntent)
        }
    }
}