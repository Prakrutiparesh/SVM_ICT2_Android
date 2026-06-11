package com.example.ict2_project

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentMarksAdapter(
    private val students: List<StudentMarksData>,
    private val maxMarks: Int,
    private val isEditable: Boolean = true  // ✅ New parameter
) : RecyclerView.Adapter<StudentMarksAdapter.ViewHolder>() {

    private val marksMap = mutableMapOf<Int, Float?>()

    init {
        students.forEach { student ->
            val existingMark = student.marks.firstOrNull()?.obtainedMarks
            marksMap[student.studentId] = existingMark
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_marks, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]

        holder.tvRollNo.text = student.rollNo.toString()
        holder.tvStudentName.text = student.studentName
        holder.tvGrno.text = student.grno

        val currentMark = marksMap[student.studentId]
        holder.etMarks.setText(currentMark?.toString() ?: "")
        holder.etMarks.hint = "Max $maxMarks"

        // ✅ Set edit mode based on isEditable flag
        holder.etMarks.isEnabled = isEditable
        holder.etMarks.isFocusable = isEditable
        holder.etMarks.isFocusableInTouchMode = isEditable

        // ✅ Change background based on edit mode
        if (!isEditable) {
            holder.etMarks.background = null
            holder.etMarks.setTextColor(holder.itemView.context.getColor(R.color.text_primary))
        }

        if (isEditable) {
            val textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val inputText = s?.toString()
                    if (inputText.isNullOrEmpty()) {
                        marksMap[student.studentId] = null
                    } else {
                        val value = inputText.toFloatOrNull()
                        if (value != null && value in 0f..maxMarks.toFloat()) {
                            marksMap[student.studentId] = value
                            holder.etMarks.error = null
                        } else if (value != null && value > maxMarks) {
                            holder.etMarks.error = "Max $maxMarks"
                            marksMap[student.studentId] = null
                        } else {
                            marksMap[student.studentId] = null
                        }
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            holder.etMarks.removeTextChangedListener(holder.currentTextWatcher)
            holder.currentTextWatcher = textWatcher
            holder.etMarks.addTextChangedListener(textWatcher)
        }
    }

    override fun getItemCount() = students.size

    fun getMarksList(): List<Triple<Int, Int, Float?>> {
        if (!isEditable) return emptyList()  // ✅ Return empty if not editable

        return students.mapNotNull { student ->
            val mark = marksMap[student.studentId]
            if (mark != null) {
                val examSubjectId = student.marks.firstOrNull()?.examSubjectId
                if (examSubjectId != null) {
                    Triple(examSubjectId, student.studentId, mark)
                } else null
            } else null
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRollNo: TextView = itemView.findViewById(R.id.tvRollNo)
        val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvGrno: TextView = itemView.findViewById(R.id.tvGrno)
        val etMarks: EditText = itemView.findViewById(R.id.etMarks)
        var currentTextWatcher: TextWatcher? = null
    }
}