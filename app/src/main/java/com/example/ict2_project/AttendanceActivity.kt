package com.example.ict2_project.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ict2_project.R
import com.example.ict2_project.adapters.StudentAttendanceAdapter
import com.example.ict2_project.api.ApiService
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.databinding.ActivityAttendanceBinding
import com.example.ict2_project.models.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var apiService: ApiService

    private var sessionList = listOf<Session>()
    private var classList = listOf<Class>()
    private var sectionList = listOf<Section>()

    private var selectedSessionId: Int? = null
    private var selectedMedium: String? = null
    private var selectedClassId: Int? = null
    private var selectedSectionId: Int? = null

    // API uses yyyy-MM-dd
    private val selectedDate: String by lazy {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private lateinit var adapter: StudentAttendanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        apiService = RetrofitClient.instance

        // Hide student list card initially
        binding.cardStudentListContainer.visibility = View.GONE

        // Show current date in dd-MM-yyyy format
        val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val displayDate = displayDateFormat.format(Calendar.getInstance().time)
        binding.tvSelectedDate.text = "Date: $displayDate"

        setupRecyclerView()
        loadSessions()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = StudentAttendanceAdapter(emptyList<Student>()) { _, _ -> }
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
    }

    private fun loadSessions() {
        apiService.getSessions().enqueue(object : Callback<List<Session>> {
            override fun onResponse(call: Call<List<Session>>, response: Response<List<Session>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    sessionList = response.body()!!
                    val sessionNames = sessionList.map { it.sessionName }
                    val adapter = ArrayAdapter(
                        this@AttendanceActivity,
                        R.layout.dropdown_item,
                        sessionNames
                    )
                    binding.spinnerSession.setAdapter(adapter)
                    if (sessionList.isNotEmpty()) {
                        binding.spinnerSession.setText(sessionNames[0], false)
                        selectedSessionId = sessionList[0].sessionId
                    }
                    binding.spinnerSession.setOnItemClickListener { _, _, position, _ ->
                        selectedSessionId = sessionList[position].sessionId
                        loadMediums()
                    }
                } else {
                    Toast.makeText(this@AttendanceActivity, "No sessions found", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Session>>, t: Throwable) {
                Toast.makeText(this@AttendanceActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun loadMediums() {
        val mediums = listOf("Gujarati", "English")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, mediums)
        binding.spinnerMedium.setAdapter(adapter)
        binding.spinnerMedium.setText(mediums[0], false)
        selectedMedium = mediums[0]
        loadClasses()
        binding.spinnerMedium.setOnItemClickListener { _, _, position, _ ->
            selectedMedium = mediums[position]
            clearClassAndSection()
            binding.cardStudentListContainer.visibility = View.GONE   // Hide card on medium change
            loadClasses()
        }
    }

    private fun clearClassAndSection() {
        selectedClassId = null
        selectedSectionId = null
        classList = emptyList()
        sectionList = emptyList()
        binding.spinnerClass.setText("", false)
        binding.spinnerSection.setText("", false)
        adapter.updateStudents(emptyList())
        binding.cardStudentListContainer.visibility = View.GONE   // Hide card
        binding.btnSubmitAttendance.isEnabled = false
    }

    private fun loadClasses() {
        apiService.getClasses(selectedMedium).enqueue(object : Callback<List<Class>> {
            override fun onResponse(call: Call<List<Class>>, response: Response<List<Class>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    classList = response.body()!!
                    val classNames = classList.map { it.className }
                    val adapter = ArrayAdapter(
                        this@AttendanceActivity,
                        R.layout.dropdown_item,
                        classNames
                    )
                    binding.spinnerClass.setAdapter(adapter)
                    binding.spinnerClass.setOnItemClickListener { _, _, position, _ ->
                        selectedClassId = classList[position].classId
                        clearSection()
                        loadSections()
                    }
                } else {
                    Toast.makeText(
                        this@AttendanceActivity,
                        "No classes found for ${selectedMedium}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Class>>, t: Throwable) {
                Toast.makeText(this@AttendanceActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun clearSection() {
        selectedSectionId = null
        sectionList = emptyList()
        binding.spinnerSection.setText("", false)
        adapter.updateStudents(emptyList())
        binding.cardStudentListContainer.visibility = View.GONE   // Hide card
        binding.btnSubmitAttendance.isEnabled = false
    }

    private fun loadSections() {
        if (selectedClassId == null) return
        apiService.getSectionsByClass(selectedClassId!!).enqueue(object : Callback<List<Section>> {
            override fun onResponse(call: Call<List<Section>>, response: Response<List<Section>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    sectionList = response.body()!!
                    val sectionNames = sectionList.map { it.sectionName }
                    val adapter = ArrayAdapter(
                        this@AttendanceActivity,
                        R.layout.dropdown_item,
                        sectionNames
                    )
                    binding.spinnerSection.setAdapter(adapter)
                    binding.spinnerSection.setOnItemClickListener { _, _, position, _ ->
                        selectedSectionId = sectionList[position].sectionId
                    }
                } else {
                    Toast.makeText(this@AttendanceActivity, "No sections found", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Section>>, t: Throwable) {
                Toast.makeText(this@AttendanceActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun setupButtons() {
        binding.btnLoadStudents.setOnClickListener {
            if (selectedSessionId == null || selectedClassId == null || selectedSectionId == null) {
                Toast.makeText(
                    this,
                    "Please select session, medium, class and section",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            loadStudents()
        }

        binding.btnSubmitAttendance.setOnClickListener {
            submitAttendance()
        }
    }

    private fun loadStudents() {
        lifecycleScope.launch {
            try {
                val students = apiService.getStudentsForAttendance(
                    sessionId = selectedSessionId!!,
                    medium = selectedMedium,
                    classId = selectedClassId,
                    sectionId = selectedSectionId
                )
                adapter.updateStudents(students)
                binding.btnSubmitAttendance.isEnabled = students.isNotEmpty()
                if (students.isNotEmpty()) {
                    binding.cardStudentListContainer.visibility =
                        View.VISIBLE   // Show card only when students exist
                } else {
                    binding.cardStudentListContainer.visibility = View.GONE
                    Toast.makeText(
                        this@AttendanceActivity,
                        "No students found for selected criteria",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AttendanceActivity,
                    "Failed to load students: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun submitAttendance() {
        val attendanceItems = adapter.getAllStatuses().map { (studentId, status) ->
            AttendanceItem(studentId, status)
        }

        val request = BulkAttendanceRequest(
            classId = selectedClassId!!,
            sectionId = selectedSectionId!!,
            sessionId = selectedSessionId!!,
            attendanceDate = selectedDate,
            attendances = attendanceItems
        )

        lifecycleScope.launch {
            try {
                val response = apiService.submitAttendance(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.alreadyExists) {
                        Toast.makeText(
                            this@AttendanceActivity,
                            "Attendance already marked for this class/section/date",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@AttendanceActivity,
                            "Attendance saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else if (response.code() == 409) {
                    Toast.makeText(
                        this@AttendanceActivity,
                        "Attendance already marked for this class",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@AttendanceActivity,
                        "Failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AttendanceActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}