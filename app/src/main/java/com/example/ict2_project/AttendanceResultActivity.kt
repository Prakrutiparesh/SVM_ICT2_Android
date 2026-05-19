package com.example.ict2_project

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        // Get data from Intent
        val students =
            intent.getSerializableExtra("students") as? List<DailyReportStudent> ?: emptyList()
        val totals = intent.getSerializableExtra("totals") as? AttendanceTotals

        // Set totals
        findViewById<TextView>(R.id.tvTotalStudents).text = "Total Students: ${students.size}"
        totals?.let {
            findViewById<TextView>(R.id.tvTotalPresent).text = "Present: ${it.totalPresent}"
            findViewById<TextView>(R.id.tvTotalAbsent).text = "Absent: ${it.totalAbsent}"
            findViewById<TextView>(R.id.tvGirlsBreakup).text =
                "Girls: P ${it.girlsPresent} | A ${it.girlsAbsent}"
            findViewById<TextView>(R.id.tvBoysBreakup).text =
                "Boys: P ${it.boysPresent} | A ${it.boysAbsent}"
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
}