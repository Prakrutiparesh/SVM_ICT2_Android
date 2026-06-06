package com.example.ict2_project

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ict2_project.models.AttendanceTotals
import com.example.ict2_project.models.DailyReportStudent

class AttendanceResultActivity : AppCompatActivity() {

    private lateinit var rvStudents: RecyclerView
    private lateinit var adapter: DailyReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_result)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Get data from Intent
        val students =
            intent.getSerializableExtra("students") as? List<DailyReportStudent> ?: emptyList()
        val totals = intent.getSerializableExtra("totals") as? AttendanceTotals

        findViewById<TextView>(R.id.tvTotalStudents).text = students.size.toString()
        totals?.let {
            findViewById<TextView>(R.id.tvTotalPresent).text = it.totalPresent.toString()
            findViewById<TextView>(R.id.tvTotalAbsent).text = it.totalAbsent.toString()
            findViewById<TextView>(R.id.tvGirlsBreakup).text =
                "Girls — Present: ${it.girlsPresent}, Absent: ${it.girlsAbsent}"
            findViewById<TextView>(R.id.tvBoysBreakup).text =
                "Boys — Present: ${it.boysPresent}, Absent: ${it.boysAbsent}"
        }

        // Set up RecyclerView
        rvStudents = findViewById(R.id.rvAttendanceList)
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.isNestedScrollingEnabled = false
        adapter = DailyReportAdapter(students)
        rvStudents.adapter = adapter

        // Show no data message if needed
        val tvNoData = findViewById<TextView>(R.id.tvNoData)
        if (students.isEmpty()) {
            rvStudents.visibility = android.view.View.GONE
            tvNoData.visibility = android.view.View.VISIBLE
        } else {
            rvStudents.visibility = android.view.View.VISIBLE
            tvNoData.visibility = android.view.View.GONE
        }
    }

    // ✅ Back button click handler
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}