package com.example.ict2_project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ict2_project.databinding.ItemTimetableBinding
import com.example.ict2_project.models.Timetable

class TimetableAdapter(private val timetableList: List<Timetable>) :
    RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimetableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timetableList[position])
    }

    override fun getItemCount() = timetableList.size

    inner class ViewHolder(private val binding: ItemTimetableBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(timetable: Timetable) {
            binding.tvDay.text = timetable.dayName           // changed from dayOfWeek
            binding.tvSubject.text = timetable.subject?.subjectName ?: "N/A"
            binding.tvTeacher.text = timetable.staff?.fullName ?: "N/A"
            binding.tvTime.text = "${timetable.startTime} - ${timetable.endTime}"
            // Room does not exist – you may hide the row or show lecture number
            binding.tvLecture.text = "Lecture ${timetable.lectureNo}"
        }
    }
}