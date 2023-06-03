package org.catrobat.paintroid.adapter

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import org.catrobat.paintroid.R
import org.catrobat.paintroid.model.Project
import org.catrobat.paintroid.projectDB

class ProjectAdapter(var projectList: ArrayList<Project>): RecyclerView.Adapter<ProjectAdapter.ItemViewHolder>() {
    private var itemClickListener: OnItemClickListener? = null

    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemImageView: ImageView = itemView.findViewById(R.id.iv_pocket_paint_project_thumbnail_image)
        val itemNameText: TextView = itemView.findViewById(R.id.tv_pocket_paint_project_name)
        val itemLastModifiedText: TextView = itemView.findViewById(R.id.tv_pocket_paint_project_lastmodified)
        val itemMoreOption : ImageView = itemView.findViewById(R.id.iv_pocket_paint_project_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pocket_paint_card_project, parent, false)
        return ItemViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = projectList[position]
        holder.itemImageView.setImageURI(Uri.parse(item.imagePreviewPath))
        holder.itemNameText.text = item.name.substringBefore(".catrobat-image")
        holder.itemLastModifiedText.text = item.lastModified
        val projectDetailsMenu = holder.itemMoreOption
        projectDetailsMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.menu_pocketpaint_project_details)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId){
                    R.id.project_details -> {
                        showDetailsDialog(view.context, position)
                        true
                    }
                    R.id.project_delete -> {
                        showDeleteDialog(view.context, position)
                        projectList.removeAt(position)
                        notifyDataSetChanged()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        holder.itemView.setOnClickListener {
            val clickedItem = projectList[position]
            val projectUri = clickedItem.path
            val projectName = clickedItem.name
            itemClickListener?.onItemClick(position, projectUri, projectName)
        }
    }

    private fun showDetailsDialog(context: Context, position: Int) {
        AlertDialog.Builder(context, R.style.PocketPaintAlertDialog)
            .setTitle(projectList[position].name.removeSuffix(".catrobat-image"))
            .setMessage("Resolution: " + projectList[position].resolution + "\n" +
                "Last modified: " + projectList[position].lastModified + "\n" +
                "Creation date: " + projectList[position].creationDate + "\n" +
                "Format: " + projectList[position].format + "\n" +
                "Size: " + projectList[position].size + "\n")
            .setPositiveButton(R.string.pocketpaint_ok) { _, _ -> null }
            .create()
            .show()
    }

    private fun showDeleteDialog(context: Context, position: Int) {
        AlertDialog.Builder(context, R.style.PocketPaintAlertDialog)
            .setTitle("Delete " + projectList[position].name.removeSuffix(".catrobat-image"))
            .setMessage("Do you really want to delete your Project?")
            .setPositiveButton("Delete"){ _, _ ->
                Toast.makeText(context, "Deleting", Toast.LENGTH_SHORT).show()
                projectDB.dao.deleteProject(projectList[position-1].id)
            }
            .setNegativeButton(R.string.cancel_button_text){ _, _ -> }
            .create()
            .show()
    }

    override fun getItemCount(): Int {
        return projectList.size
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int, projectUri: String, projectName: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        itemClickListener = listener
    }
}