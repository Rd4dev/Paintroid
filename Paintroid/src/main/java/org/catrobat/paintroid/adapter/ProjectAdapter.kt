package org.catrobat.paintroid.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.catrobat.paintroid.R
import org.catrobat.paintroid.model.Project

class ProjectAdapter(var projectList: ArrayList<Project>): RecyclerView.Adapter<ProjectAdapter.ItemViewHolder>() {
    private var itemClickListener: OnItemClickListener? = null

    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemImageView: ImageView = itemView.findViewById(R.id.iv_pocket_paint_project_thumbnail_image)
        val itemNameText: TextView = itemView.findViewById(R.id.tv_pocket_paint_project_name)
        val itemLastModifiedText: TextView = itemView.findViewById(R.id.tv_pocket_paint_project_lastmodified)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pocket_paint_card_project, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = projectList[position]
        holder.itemImageView.setImageURI(Uri.parse(item.imagePreviewPath))
        holder.itemNameText.text = item.name.substringBefore(".catrobat-image")
        holder.itemLastModifiedText.text = item.lastModified

        /*holder.itemView.setOnClickListener {
            val clickedItem = projectList[position]
            val currentPosition = position
            val projecturi = clickedItem.path
        }*/

        holder.itemView.setOnClickListener {
            val clickedItem = projectList[position]
            val currentPosition = position
            val projecturi = clickedItem.path
            itemClickListener?.onItemClick(currentPosition, projecturi)
        }
    }

    override fun getItemCount(): Int {
        return projectList.size
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int, projectUri: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        itemClickListener = listener
    }
}