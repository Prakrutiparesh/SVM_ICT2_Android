package com.example.ict2_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ict2_project.R
import com.example.ict2_project.databinding.ItemStudentAttendanceBinding
import com.example.ict2_project.models.Student
import com.google.android.material.button.MaterialButton

class StudentAttendanceAdapter(
    private var students: List<Student>,
    private val onStatusChanged: (studentId: Int, status: String) -> Unit
) : RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder>() {

    private val selectedStatuses = mutableMapOf<Int, String>().apply {
        students.forEach { put(it.studentId, "Present") }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student)

        // Remove previous listeners to avoid conflicts
        holder.binding.btnPresent.setOnClickListener(null)
        holder.binding.btnAbsent.setOnClickListener(null)

        // Set visual state based on current status
        updateButtonAppearance(
            holder.binding.btnPresent,
            holder.binding.btnAbsent,
            selectedStatuses[student.studentId] ?: "Present"
        )

        holder.binding.btnPresent.setOnClickListener {
            if (selectedStatuses[student.studentId] != "Present") {
                selectedStatuses[student.studentId] = "Present"
                updateButtonAppearance(
                    holder.binding.btnPresent,
                    holder.binding.btnAbsent,
                    "Present"
                )
                onStatusChanged(student.studentId, "Present")
            }
        }

        holder.binding.btnAbsent.setOnClickListener {
            if (selectedStatuses[student.studentId] != "Absent") {
                selectedStatuses[student.studentId] = "Absent"
                updateButtonAppearance(
                    holder.binding.btnPresent,
                    holder.binding.btnAbsent,
                    "Absent"
                )
                onStatusChanged(student.studentId, "Absent")
            }
        }
    }

    private fun updateButtonAppearance(
        btnPresent: MaterialButton,
        btnAbsent: MaterialButton,
        status: String
    ) {
        val context = btnPresent.context
        when (status) {
            "Present" -> {
                btnPresent.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.present_green
                    )
                )
                btnPresent.setTextColor(Color.WHITE)
                btnAbsent.setBackgroundColor(Color.TRANSPARENT)
                btnAbsent.setTextColor(ContextCompat.getColor(context, R.color.absent_red))
            }

            "Absent" -> {
                btnAbsent.setBackgroundColor(ContextCompat.getColor(context, R.color.absent_red))
                btnAbsent.setTextColor(Color.WHITE)
                btnPresent.setBackgroundColor(Color.TRANSPARENT)
                btnPresent.setTextColor(ContextCompat.getColor(context, R.color.present_green))
            }
        }
    }

    override fun getItemCount() = students.size

    fun updateStudents(newStudents: List<Student>) {
        students = newStudents
        selectedStatuses.clear()
        students.forEach { selectedStatuses[it.studentId] = "Present" }
        notifyDataSetChanged()
    }

    fun getAllStatuses(): Map<Int, String> = selectedStatuses.toMap()

    inner class ViewHolder(val binding: ItemStudentAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) {
            binding.tvStudentName.text = student.fullName
            binding.tvRollNo.text = "Roll No: ${student.rollNo}"
            binding.tvAvatarInitial.text =
                student.fullName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        }
    }
}