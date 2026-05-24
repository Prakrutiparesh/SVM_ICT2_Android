package com.example.ict2_project.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ict2_project.R
import com.example.ict2_project.api.ApiService
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.databinding.ActivityAttendanceBinding
import com.example.ict2_project.models.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        apiService = RetrofitClient.instance

        val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val displayDate = displayDateFormat.format(Calendar.getInstance().time)
        binding.tvSelectedDate.text = "Date: $displayDate"

        loadSessions()
        setupButtons()
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
                        loadMediums()
                    }

                    binding.spinnerSession.setOnItemClickListener { _, _, position, _ ->
                        selectedSessionId = sessionList[position].sessionId
                        clearClassAndSection()
                        loadMediums()  // mediums reload, jo class bhi reload karega naye session ke liye
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

        loadClasses()   // medium set hone ke baad classes load

        binding.spinnerMedium.setOnItemClickListener { _, _, position, _ ->
            selectedMedium = mediums[position]
            clearClassAndSection()
            loadClasses()   // naye medium ke liye classes reload
        }
    }

    private fun clearClassAndSection() {
        selectedClassId = null
        selectedSectionId = null
        classList = emptyList()
        sectionList = emptyList()

        binding.spinnerClass.setText("", false)
        binding.spinnerSection.setText("", false)

        binding.spinnerClass.setAdapter(
            ArrayAdapter(this, R.layout.dropdown_item, emptyList<String>())
        )
        binding.spinnerSection.setAdapter(
            ArrayAdapter(this, R.layout.dropdown_item, emptyList<String>())
        )
    }

    private fun loadClasses() {
        if (selectedSessionId == null || selectedMedium.isNullOrEmpty()) return

        // Pehle saare classes fetch karo medium ke hisaab se (jaise DailyAttendanceReportActivity mein)
        apiService.getClasses(selectedMedium!!).enqueue(object : Callback<List<Class>> {
            override fun onResponse(call: Call<List<Class>>, response: Response<List<Class>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    // 🟢 Filter classes by selected session (same as DailyAttendanceReportActivity)
                    val allClasses = response.body()!!
                    classList = allClasses.filter {
                        it.sessionId == selectedSessionId &&
                                it.medium.equals(selectedMedium, ignoreCase = true)
                    }

                    val classNames = classList.map { it.className }
                    val adapter = ArrayAdapter(
                        this@AttendanceActivity,
                        R.layout.dropdown_item,
                        classNames
                    )
                    binding.spinnerClass.setAdapter(adapter)

                    if (classNames.isEmpty()) {
                        Toast.makeText(
                            this@AttendanceActivity,
                            "No classes found for this session & medium",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.spinnerClass.setOnItemClickListener { _, _, position, _ ->
                        selectedClassId = classList[position].classId
                        clearSection()
                        loadSections()
                    }
                } else {
                    Toast.makeText(
                        this@AttendanceActivity,
                        "No classes found for $selectedMedium",
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
        binding.spinnerSection.setAdapter(
            ArrayAdapter(this, R.layout.dropdown_item, emptyList<String>())
        )
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
            val className = classList.find { it.classId == selectedClassId }?.className ?: ""
            val sectionName =
                sectionList.find { it.sectionId == selectedSectionId }?.sectionName ?: ""
            val intent = Intent(this, StudentListActivity::class.java).apply {
                putExtra("SESSION_ID", selectedSessionId!!)
                putExtra("CLASS_ID", selectedClassId!!)
                putExtra("SECTION_ID", selectedSectionId!!)
                putExtra("MEDIUM", selectedMedium)
                putExtra("CLASS_NAME", className)
                putExtra("SECTION_NAME", sectionName)
            }
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}