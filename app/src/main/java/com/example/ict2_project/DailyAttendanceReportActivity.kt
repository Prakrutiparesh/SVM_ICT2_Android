package com.example.ict2_project

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ict2_project.api.ApiService
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.models.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DailyAttendanceReportActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    // SAME AS MONTHLY REPORT
    private lateinit var sessionSpinner: AutoCompleteTextView
    private lateinit var mediumSpinner: AutoCompleteTextView
    private lateinit var classSpinner: AutoCompleteTextView
    private lateinit var sectionSpinner: AutoCompleteTextView
    private lateinit var etDate: AutoCompleteTextView

    private lateinit var btnLoad: Button
    private lateinit var rvStudents: RecyclerView
    private lateinit var tvNoData: TextView

    private lateinit var cardTotals: View
    private lateinit var tvTotalStudents: TextView
    private lateinit var tvTotalPresent: TextView
    private lateinit var tvTotalAbsent: TextView
    private lateinit var tvGirlsBreakup: TextView
    private lateinit var tvBoysBreakup: TextView

    private var sessionList = listOf<Session>()
    private var classList = listOf<Class>()
    private var sectionList = listOf<Section>()

    private lateinit var adapter: DailyReportAdapter

    private var selectedDateForApi: String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private var selectedDateForDisplay: String =
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_attendance_report)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        apiService = RetrofitClient.instance

        initViews()
        setupSpinners()
        setupDatePicker()
        loadSessions()

        btnLoad.setOnClickListener {
            loadReport()
        }
        rvStudents.setHasFixedSize(true)
    }

    private fun initViews() {

        sessionSpinner = findViewById(R.id.spinnerSession)
        mediumSpinner = findViewById(R.id.spinnerMedium)
        classSpinner = findViewById(R.id.spinnerClass)
        sectionSpinner = findViewById(R.id.spinnerSection)
        etDate = findViewById(R.id.etDate)

        btnLoad = findViewById(R.id.btnLoadReport)

        rvStudents = findViewById(R.id.rvAttendanceList)
        tvNoData = findViewById(R.id.tvNoData)

        cardTotals = findViewById(R.id.cardTotals)

        tvTotalStudents = findViewById(R.id.tvTotalStudents)
        tvTotalPresent = findViewById(R.id.tvTotalPresent)
        tvTotalAbsent = findViewById(R.id.tvTotalAbsent)
        tvGirlsBreakup = findViewById(R.id.tvGirlsBreakup)
        tvBoysBreakup = findViewById(R.id.tvBoysBreakup)

        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.setHasFixedSize(true)

        adapter = DailyReportAdapter(emptyList())

        rvStudents.adapter = adapter
    }

    private fun setupSpinners() {

        // Medium Spinner
        val mediums = listOf("Gujarati", "English")

        val mediumAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_item,
            mediums
        )

        mediumSpinner.setAdapter(mediumAdapter)

        // Medium select hone ke baad class load
        mediumSpinner.setOnItemClickListener { _, _, _, _ ->

            if (sessionSpinner.text.toString().isNotEmpty()) {
                loadClasses()
            } else {
                toast("Please select session first")
                mediumSpinner.setText("", false)
            }
        }

        // Class select hone ke baad section load
        classSpinner.setOnItemClickListener { _, _, _, _ ->
            loadSections()
        }
    }

    private fun setupDatePicker() {

        etDate.setText(selectedDateForDisplay, false)

        etDate.setOnClickListener {

            val parts = selectedDateForDisplay.split("-")

            val day = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val year = parts[2].toInt()

            DatePickerDialog(
                this,
                { _, y, m, d ->

                    selectedDateForDisplay =
                        String.format("%02d-%02d-%04d", d, m + 1, y)

                    selectedDateForApi =
                        String.format("%04d-%02d-%02d", y, m + 1, d)

                    etDate.setText(selectedDateForDisplay, false)

                },
                year,
                month,
                day
            ).show()
        }
    }

    private fun loadSessions() {

        apiService.getSessions().enqueue(
            object : Callback<List<Session>> {

                override fun onResponse(
                    call: Call<List<Session>>,
                    response: Response<List<Session>>
                ) {

                    if (response.isSuccessful) {

                        sessionList = response.body() ?: emptyList()

                        val sessionNames =
                            sessionList.map { it.sessionName }

                        val adapter = ArrayAdapter(
                            this@DailyAttendanceReportActivity,
                            R.layout.dropdown_item,
                            sessionNames
                        )

                        sessionSpinner.setAdapter(adapter)

                        sessionSpinner.setOnItemClickListener { _, _, _, _ ->

                            // reset old data
                            classSpinner.setText("", false)
                            sectionSpinner.setText("", false)

                            classList = emptyList()
                            sectionList = emptyList()

                            // clear adapters
                            classSpinner.setAdapter(
                                ArrayAdapter(
                                    this@DailyAttendanceReportActivity,
                                    R.layout.dropdown_item,
                                    emptyList<String>()
                                )
                            )

                            sectionSpinner.setAdapter(
                                ArrayAdapter(
                                    this@DailyAttendanceReportActivity,
                                    R.layout.dropdown_item,
                                    emptyList<String>()
                                )
                            )

                            // medium selected ho tabhi class load
                            if (mediumSpinner.text.toString().isNotEmpty()) {
                                loadClasses()
                            }
                        }

                    } else {
                        toast("Failed to load sessions")
                    }
                }

                override fun onFailure(
                    call: Call<List<Session>>,
                    t: Throwable
                ) {

                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun loadClasses() {

        val selectedSession =
            sessionList.find {
                it.sessionName == sessionSpinner.text.toString()
            } ?: return

        val medium = mediumSpinner.text.toString()

        if (medium.isEmpty()) {
            toast("Select medium")
            return
        }

        // CLEAR OLD DATA
        classSpinner.setText("", false)
        sectionSpinner.setText("", false)

        classList = emptyList()
        sectionList = emptyList()

        // CLEAR OLD ADAPTERS
        classSpinner.setAdapter(
            ArrayAdapter(
                this,
                R.layout.dropdown_item,
                emptyList<String>()
            )
        )

        sectionSpinner.setAdapter(
            ArrayAdapter(
                this,
                R.layout.dropdown_item,
                emptyList<String>()
            )
        )

        apiService.getClasses(medium)
            .enqueue(object : Callback<List<Class>> {

                override fun onResponse(
                    call: Call<List<Class>>,
                    response: Response<List<Class>>
                ) {

                    if (response.isSuccessful) {

                        classList =
                            response.body()?.filter {

                                it.sessionId == selectedSession.sessionId &&
                                        it.medium.equals(
                                            medium,
                                            ignoreCase = true
                                        )

                            } ?: emptyList()

                        val classNames =
                            classList.map { it.className }

                        val adapter = ArrayAdapter(
                            this@DailyAttendanceReportActivity,
                            R.layout.dropdown_item,
                            classNames
                        )

                        classSpinner.setAdapter(adapter)

                        if (classNames.isEmpty()) {
                            toast("No classes found")
                        }

                    } else {

                        toast("Failed to load classes")
                    }
                }

                override fun onFailure(
                    call: Call<List<Class>>,
                    t: Throwable
                ) {

                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun loadSections() {

        val selectedClass =
            classList.find {
                it.className == classSpinner.text.toString()
            } ?: return

        apiService.getSectionsByClass(selectedClass.classId)
            .enqueue(object : Callback<List<Section>> {

                override fun onResponse(
                    call: Call<List<Section>>,
                    response: Response<List<Section>>
                ) {

                    if (response.isSuccessful) {

                        sectionList = response.body() ?: emptyList()

                        val sectionNames =
                            sectionList.map { it.sectionName }

                        val adapter = ArrayAdapter(
                            this@DailyAttendanceReportActivity,
                            R.layout.dropdown_item,
                            sectionNames
                        )

                        sectionSpinner.setAdapter(adapter)

                    } else {

                        toast("Failed to load sections")
                    }
                }

                override fun onFailure(
                    call: Call<List<Section>>,
                    t: Throwable
                ) {

                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun loadReport() {
        val selectedSession = sessionList.find { it.sessionName == sessionSpinner.text.toString() }
        if (selectedSession == null) {
            toast("Select session")
            return
        }

        val selectedClass = classList.find { it.className == classSpinner.text.toString() }
        if (selectedClass == null) {
            toast("Select class")
            return
        }

        val selectedSection = sectionList.find { it.sectionName == sectionSpinner.text.toString() }
        if (selectedSection == null) {
            toast("Select section")
            return
        }

        val medium = mediumSpinner.text.toString()

        lifecycleScope.launch {
            try {
                val response = apiService.getDailyAttendanceReport(
                    selectedSession.sessionId,
                    medium,
                    selectedClass.classId,
                    selectedSection.sectionId,
                    selectedDateForApi
                )

                if (response.isSuccessful && response.body() != null) {
                    val report = response.body()!!
                    val intent = Intent(
                        this@DailyAttendanceReportActivity,
                        AttendanceResultActivity::class.java
                    )
                    if (report.isAttendanceMarked && !report.students.isNullOrEmpty()) {
                        // ✅ Explicit cast to Serializable to avoid overload ambiguity
                        intent.putExtra(
                            "students",
                            ArrayList(report.students) as java.io.Serializable
                        )
                        intent.putExtra("totals", report.totals as java.io.Serializable)
                    } else {
                        intent.putExtra(
                            "students",
                            ArrayList<DailyReportStudent>() as java.io.Serializable
                        )
                        intent.putExtra("totals", null as java.io.Serializable?)
                    }
                    startActivity(intent)
                } else {
                    toast("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                toast("Network error: ${e.message}")
            }
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}