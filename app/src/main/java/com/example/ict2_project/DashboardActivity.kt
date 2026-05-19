package com.example.ict2_project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.ict2_project.activities.AttendanceActivity
import com.example.ict2_project.activities.AttendanceReportActivity   // new activity
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvWelcome: TextView
    private lateinit var cardAttendance: MaterialCardView
    private lateinit var cardTimetable: MaterialCardView
    private lateinit var cardAttendanceReport: MaterialCardView   // <-- add this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        tvWelcome = findViewById(R.id.tvWelcome)
        val fullName = sharedPreferences.getString("fullName", "User")
        tvWelcome.text = "Welcome, $fullName!"

        // Find cards
        cardAttendance = findViewById(R.id.cardAttendance)
        cardTimetable = findViewById(R.id.cardTimetable)
        cardAttendanceReport = findViewById(R.id.cardAttendanceReport)   // <-- find

        // Set click listeners
        cardAttendance.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }

        cardTimetable.setOnClickListener {
            startActivity(Intent(this, TimeTableActivity::class.java))
        }

        // NEW: Attendance Report click
        cardAttendanceReport.setOnClickListener {
            startActivity(Intent(this, AttendanceReportActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    Boolean::class.javaPrimitiveType
                )
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sharedPreferences.edit().clear().apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}