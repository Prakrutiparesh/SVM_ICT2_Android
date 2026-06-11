package com.example.ict2_project.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ict2_project.R
import com.example.ict2_project.adapters.StudentAttendanceAdapter
import com.example.ict2_project.api.ApiService
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.models.AttendanceItem
import com.example.ict2_project.models.BulkAttendanceRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StudentListActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var rvStudents: RecyclerView
    private lateinit var btnSubmit: MaterialButton
    private lateinit var tvInfo: TextView
    private lateinit var cardSelectAll: MaterialCardView
    private lateinit var btnToggleAll: MaterialButton  // Toggle button
    private lateinit var tvAttendanceSummary: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: StudentAttendanceAdapter

    private var sessionId: Int = 0
    private var classId: Int = 0
    private var sectionId: Int = 0
    private var medium: String? = null
    private var isAllPresent = true  // Current state for toggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        apiService = RetrofitClient.instance

        // Get data from intent
        sessionId = intent.getIntExtra("SESSION_ID", 0)
        classId = intent.getIntExtra("CLASS_ID", 0)
        sectionId = intent.getIntExtra("SECTION_ID", 0)
        medium = intent.getStringExtra("MEDIUM")
        val className = intent.getStringExtra("CLASS_NAME") ?: ""
        val sectionName = intent.getStringExtra("SECTION_NAME") ?: ""

        // Initialize views
        tvInfo = findViewById(R.id.tvInfo)
        tvInfo.text = "Class: $className | Section: $sectionName | Medium: ${medium ?: "All"}"

        rvStudents = findViewById(R.id.rvStudents)
        btnSubmit = findViewById(R.id.btnSubmitAttendance)
        progressBar = findViewById(R.id.progressBar)
        cardSelectAll = findViewById(R.id.cardSelectAll)
        btnToggleAll = findViewById(R.id.btnToggleAll)  // Toggle button
        tvAttendanceSummary = findViewById(R.id.tvAttendanceSummary)

        setupRecyclerView()
        loadStudents()

        btnSubmit.setOnClickListener { submitAttendance() }
    }

    private fun setupRecyclerView() {
        adapter = StudentAttendanceAdapter(emptyList()) { _, _ -> }
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter
    }

    // ========== TOGGLE BUTTON SETUP - Single Button for All ==========
    private fun setupToggleButton() {
        btnToggleAll.setOnClickListener {
            if (isAllPresent) {
                // Currently Present -> Change to Absent
                adapter.setAllStatuses("Absent")
                btnToggleAll.text = "A"
                btnToggleAll.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.absent_red)
                isAllPresent = false
            } else {
                // Currently Absent -> Change to Present
                adapter.setAllStatuses("Present")
                btnToggleAll.text = "P"
                btnToggleAll.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.present_green)
                isAllPresent = true
            }
            updateSummary()
            Toast.makeText(
                this,
                if (isAllPresent) "All students marked as Present" else "All students marked as Absent",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateSummary() {
        val statuses = adapter.getAllStatuses()
        val presentCount = statuses.values.count { it == "Present" }
        val absentCount = statuses.values.count { it == "Absent" }
        tvAttendanceSummary.text = "P:$presentCount | A:$absentCount"
        tvAttendanceSummary.visibility = android.view.View.VISIBLE
        cardSelectAll.visibility = android.view.View.VISIBLE
    }

    private fun loadStudents() {
        progressBar.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            try {
                val students = apiService.getStudentsForAttendance(
                    sessionId = sessionId,
                    medium = medium,
                    classId = classId,
                    sectionId = sectionId
                )
                adapter.updateStudents(students)
                if (students.isNotEmpty()) {
                    setupToggleButton()  // Setup toggle button instead of separate buttons
                    updateSummary()
                }
                if (students.isEmpty()) {
                    Toast.makeText(
                        this@StudentListActivity,
                        "No students found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun submitAttendance() {
        val attendanceItems = adapter.getAllStatuses().map { (studentId, status) ->
            AttendanceItem(studentId, status)
        }
        val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val request = BulkAttendanceRequest(
            classId = classId,
            sectionId = sectionId,
            sessionId = sessionId,
            attendanceDate = selectedDate,
            attendances = attendanceItems
        )

        progressBar.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.submitAttendance(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.alreadyExists) {
                        Toast.makeText(
                            this@StudentListActivity,
                            "Attendance already marked for today",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@StudentListActivity,
                            "Attendance saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else if (response.code() == 409) {
                    Toast.makeText(
                        this@StudentListActivity,
                        "Attendance already marked",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@StudentListActivity,
                        "Failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                progressBar.visibility = android.view.View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}