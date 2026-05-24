package com.example.ict2_project.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.databinding.ItemUpdateBinding
import com.example.ict2_project.models.Updates
import java.text.SimpleDateFormat
import java.util.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.ict2_project.R

class UpdatesAdapter(
    private val updatesList: List<Updates>
) : RecyclerView.Adapter<UpdatesAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val baseUrl = RetrofitClient.getBaseUrl()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUpdateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val update = updatesList[position]
        holder.binding.tvTitle.text = update.title ?: "No Title"
        holder.binding.tvDescription.text = update.description ?: "No Description"

        val formattedDate = update.createdAt?.let { raw ->
            try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = input.parse(raw)
                dateFormat.format(date)
            } catch (e: Exception) {
                raw
            }
        } ?: ""
        holder.binding.tvDate.text = formattedDate

        val filePath = update.filePath
        if (!filePath.isNullOrEmpty()) {
            holder.binding.llAttachment.visibility = View.VISIBLE

            val cleanFilePath = filePath.trimStart('/')
            val fullUrl = baseUrl + cleanFilePath

            android.util.Log.d("UpdatesAdapter", "fullUrl: $fullUrl")

            if (filePath.endsWith(".jpg", ignoreCase = true) ||
                filePath.endsWith(".png", ignoreCase = true) ||
                filePath.endsWith(".jpeg", ignoreCase = true)
            ) {
                holder.binding.ivAttachmentIcon.load(fullUrl) {
                    placeholder(R.drawable.ic_attachment)
                    error(R.drawable.ic_attachment)
                }
            } else {
                // PDF ya any other file: bhi paperclip hi dikhao (optional)
                holder.binding.ivAttachmentIcon.setImageResource(R.drawable.ic_attachment)
            }

            holder.binding.llAttachment.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.binding.llAttachment.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = updatesList.size

    class ViewHolder(val binding: ItemUpdateBinding) : RecyclerView.ViewHolder(binding.root)
}