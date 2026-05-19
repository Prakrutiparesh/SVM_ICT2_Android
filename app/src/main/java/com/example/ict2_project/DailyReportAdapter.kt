package com.example.ict2_project

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ict2_project.models.DailyReportStudent

class DailyReportAdapter(private var students: List<DailyReportStudent>) :
    RecyclerView.Adapter<DailyReportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_report_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.tvName.text = student.fullName
        holder.tvRoll.text =
            "Roll No: ${student.rollNo}"
        holder.tvStatus.text = student.status

        // Background and text color for status
        if (student.status.equals("Present", true)) {
            holder.tvStatus.text = "Present"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_present)
        } else {
            holder.tvStatus.text = "Absent"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_absent)
        }
    }

    override fun getItemCount() = students.size

    fun updateData(newList: List<DailyReportStudent>) {
        students = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvRoll: TextView = itemView.findViewById(R.id.tvRollNo)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }
}