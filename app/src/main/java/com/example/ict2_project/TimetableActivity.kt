package com.example.ict2_project

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.databinding.ActivityTimetableBinding
import com.example.ict2_project.models.Class
import com.example.ict2_project.models.Section
import com.example.ict2_project.models.Session
import com.example.ict2_project.models.Staff
import com.example.ict2_project.models.Timetable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan

class TimeTableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimetableBinding

    private val sessions = mutableListOf<Session>()
    private val classes = mutableListOf<Class>()
    private val sections = mutableListOf<Section>()
    private val mediums = listOf("Gujarati", "English")

    private var selectedSessionId: Int? = null
    private var selectedClassId: Int? = null
    private var selectedSectionId: Int? = null
    private var selectedMedium: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupSpinners()

        binding.btnFetchTimetable.setOnClickListener {
            if (selectedSessionId == null || selectedClassId == null || selectedSectionId == null || selectedMedium == null) {
                Toast.makeText(
                    this,
                    "Select Medium, Session, Class and Section",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            fetchTimetable(selectedSessionId!!, selectedClassId!!, selectedSectionId!!)
        }
    }

    private fun setupSpinners() {
        val mediumAdapter = ArrayAdapter(this, R.layout.dropdown_item, mediums)
        (binding.spinnerMedium as? AutoCompleteTextView)?.setAdapter(mediumAdapter)
        binding.spinnerMedium.setOnItemClickListener { _, _, position, _ ->
            selectedMedium = mediums[position]
            selectedClassId = null
            selectedSectionId = null
            (binding.spinnerClass as? AutoCompleteTextView)?.text?.clear()
            (binding.spinnerSection as? AutoCompleteTextView)?.text?.clear()
            fetchClassesByMedium(selectedMedium!!)
        }

        fetchSessions()

        binding.spinnerClass.setOnItemClickListener { _, _, position, _ ->
            if (position < classes.size) {
                selectedClassId = classes[position].classId
                fetchSectionsByClass(selectedClassId!!)
            }
        }

        binding.spinnerSession.setOnItemClickListener { _, _, position, _ ->
            if (position < sessions.size) {
                selectedSessionId = sessions[position].sessionId
            }
        }

        binding.spinnerSection.setOnItemClickListener { _, _, position, _ ->
            if (position < sections.size) {
                selectedSectionId = sections[position].sectionId
            }
        }
    }

    private fun fetchSessions() {
        RetrofitClient.instance.getSessions().enqueue(object : Callback<List<Session>> {
            override fun onResponse(call: Call<List<Session>>, response: Response<List<Session>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    sessions.clear()
                    sessions.addAll(response.body()!!)
                    val names = sessions.map { it.sessionName }
                    val adapter = ArrayAdapter(
                        this@TimeTableActivity,
                        R.layout.dropdown_item,
                        names
                    )
                    (binding.spinnerSession as? AutoCompleteTextView)?.setAdapter(adapter)
                } else {
                    showError("No sessions found")
                }
            }

            override fun onFailure(call: Call<List<Session>>, t: Throwable) {
                showError("Failed to load sessions: ${t.message}")
            }
        })
    }

    private fun fetchClassesByMedium(medium: String) {
        RetrofitClient.instance.getClassesByMedium(medium).enqueue(object : Callback<List<Class>> {
            override fun onResponse(call: Call<List<Class>>, response: Response<List<Class>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    classes.clear()
                    classes.addAll(response.body()!!)
                    val classNames = classes.map { it.className }
                    val adapter = ArrayAdapter(
                        this@TimeTableActivity,
                        R.layout.dropdown_item,
                        classNames
                    )
                    (binding.spinnerClass as? AutoCompleteTextView)?.setAdapter(adapter)
                } else {
                    showError("No classes found for $medium medium")
                }
            }

            override fun onFailure(call: Call<List<Class>>, t: Throwable) {
                showError("Failed to load classes: ${t.message}")
            }
        })
    }

    private fun fetchSectionsByClass(classId: Int) {
        RetrofitClient.instance.getSectionsByClass(classId)
            .enqueue(object : Callback<List<Section>> {
                override fun onResponse(
                    call: Call<List<Section>>,
                    response: Response<List<Section>>
                ) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        sections.clear()
                        sections.addAll(response.body()!!)
                        val sectionNames = sections.map { it.sectionName }
                        val adapter = ArrayAdapter(
                            this@TimeTableActivity,
                            R.layout.dropdown_item,
                            sectionNames
                        )
                        (binding.spinnerSection as? AutoCompleteTextView)?.setAdapter(adapter)
                    } else {
                        showError("No sections found for this class")
                    }
                }

                override fun onFailure(call: Call<List<Section>>, t: Throwable) {
                    showError("Failed to load sections: ${t.message}")
                }
            })
    }

    private fun fetchTimetable(sessionId: Int, classId: Int, sectionId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tableTimetable.visibility = View.GONE
        binding.tvNoData.visibility = View.GONE

        RetrofitClient.instance.getTeacherMapping(sessionId, classId)
            .enqueue(object : Callback<Map<Int, Staff>> {
                override fun onResponse(
                    call: Call<Map<Int, Staff>>,
                    mappingResponse: Response<Map<Int, Staff>>
                ) {
                    val teacherMapping = mappingResponse.body() ?: emptyMap()

                    RetrofitClient.instance.getTimetables(sessionId, classId, sectionId)
                        .enqueue(object : Callback<List<Timetable>> {
                            override fun onResponse(
                                call: Call<List<Timetable>>,
                                response: Response<List<Timetable>>
                            ) {
                                binding.progressBar.visibility = View.GONE
                                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                    val timetables = response.body()!!
                                    timetables.forEach { entry ->
                                        if (entry.staff == null && entry.subjectId != null && teacherMapping.containsKey(
                                                entry.subjectId
                                            )
                                        ) {
                                            entry.staff = teacherMapping[entry.subjectId]
                                        }
                                    }
                                    displayTable(timetables)
                                } else {
                                    binding.tvNoData.visibility = View.VISIBLE
                                    binding.tvNoData.text = "No timetable found"
                                }
                            }

                            override fun onFailure(call: Call<List<Timetable>>, t: Throwable) {
                                binding.progressBar.visibility = View.GONE
                                showError("Network error: ${t.message}")
                                binding.tvNoData.visibility = View.VISIBLE
                            }
                        })
                }

                override fun onFailure(call: Call<Map<Int, Staff>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    showError("Failed to load teacher mapping")
                    binding.tvNoData.visibility = View.VISIBLE
                }
            })
    }

    private fun displayTable(timetables: List<Timetable>) {
        binding.tableTimetable.removeAllViews()
        binding.tableTimetable.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE

        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val allLectures = timetables.map { it.lectureNo }.distinct().sorted()

        // Header
        val headerRow = TableRow(this)
        headerRow.addView(createHeaderCell("Period"))
        for (day in dayOrder) {
            headerRow.addView(createHeaderCell(day))
        }
        binding.tableTimetable.addView(headerRow)

        // Data rows
        var rowIndex = 0
        for (lecture in allLectures) {
            val dataRow = TableRow(this)

            // Period cell (3 lines)
            val periodText = "Lecture $lecture\n\n"   // three lines: period, blank, blank
            dataRow.addView(createDataCell(periodText, rowIndex, isPeriodCell = true))

            // Day cells
            for (day in dayOrder) {
                val entry = timetables.find { it.dayName == day && it.lectureNo == lecture }
                val cellText = buildCellText(entry)   // returns 3 lines
                dataRow.addView(createDataCell(cellText, rowIndex, isPeriodCell = false))
            }

            binding.tableTimetable.addView(dataRow)
            rowIndex++
        }
    }

    private fun buildCellText(entry: Timetable?): String {
        if (entry == null) return "\n\n"   // empty 3 lines
        if (entry.isBreak == true) return "BREAK\n\n"   // BREAK + two empty lines

        val subject = entry.subject?.subjectName ?: ""
        val teacher = entry.staff?.fullName ?: ""
        val start = formatTime(entry.startTime)
        val end = formatTime(entry.endTime)
        val time = "$start - $end"

        return "$subject\n$time\n$teacher"
    }

    private fun createHeaderCell(text: String): TextView {
        return TextView(this).apply {
            setText(text)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1E3A8A"))
            setPadding(20, 16, 20, 16)
            textSize = 14f
            gravity = Gravity.CENTER
            val border = android.graphics.drawable.GradientDrawable()
            border.setStroke(1, Color.parseColor("#DDDDDD"))
            border.setColor(Color.parseColor("#1E3A8A"))
            background = border
        }
    }

    private fun createDataCell(
        text: String,
        rowIndex: Int,
        isPeriodCell: Boolean = false
    ): TextView {
        val bgColor =
            if (rowIndex % 2 == 0) Color.parseColor("#FFFFFF") else Color.parseColor("#F5F8FF")

        return TextView(this).apply {
            setTextColor(Color.BLACK)
            setBackgroundColor(bgColor)
            setPadding(20, 12, 20, 12)
            gravity = Gravity.CENTER
            maxLines = 3
            ellipsize = android.text.TextUtils.TruncateAt.END

            if (isPeriodCell) {
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setText(text)
            } else {
                textSize = 13f
                val lines = text.split("\n")
                if (lines.isNotEmpty() && lines[0].isNotBlank()) {
                    val spannable = SpannableStringBuilder()
                    spannable.append(lines[0])
                    spannable.setSpan(
                        StyleSpan(android.graphics.Typeface.BOLD),
                        0, lines[0].length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    for (i in 1 until lines.size) {
                        spannable.append("\n")
                        spannable.append(lines[i])
                    }
                    setText(spannable)
                } else {
                    setText(text)
                }
            }

            val border = android.graphics.drawable.GradientDrawable()
            border.setStroke(1, Color.parseColor("#DDDDDD"))
            border.setColor(bgColor)
            background = border
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun formatTime(time: String): String {
        return if (time.length >= 5) time.substring(0, 5) else time
    }
}