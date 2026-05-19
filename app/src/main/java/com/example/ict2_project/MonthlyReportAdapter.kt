package com.example.ict2_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ict2_project.models.MonthlyReportStudent
import java.util.*

class MonthlyReportAdapter(
    private var students: List<MonthlyReportStudent>,
    private var dates: List<Date>
) : RecyclerView.Adapter<MonthlyReportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.tvRollName.text = "${student.rollNo} - ${student.studentName}"

        holder.llStatusContainer.removeAllViews()
        for (daily in student.dailyStatus) {
            val tv = TextView(holder.itemView.context).apply {
                text = daily.status
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = android.view.Gravity.CENTER
                setTextColor(if (daily.status == "P") android.graphics.Color.GREEN else android.graphics.Color.RED)
                textSize = 14f
            }
            holder.llStatusContainer.addView(tv)
        }
    }

    override fun getItemCount() = students.size

    fun updateData(newStudents: List<MonthlyReportStudent>, newDates: List<Date>) {
        students = newStudents
        dates = newDates
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRollName: TextView = itemView.findViewById(R.id.tvRollName)
        val llStatusContainer: LinearLayout = itemView.findViewById(R.id.llStatusContainer)
    }
}