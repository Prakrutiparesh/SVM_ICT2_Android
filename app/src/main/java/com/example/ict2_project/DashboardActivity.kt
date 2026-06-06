package com.example.ict2_project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ict2_project.activities.AttendanceActivity
import com.example.ict2_project.activities.UpdatesListActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvWelcome: TextView
    private lateinit var cardAttendance: View
    private lateinit var cardTimetable: View
    private lateinit var cardAttendanceReport: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val fullName = sharedPreferences.getString("fullName", "Teacher") ?: "Teacher"
        val greeting = getGreeting()

        tvWelcome = findViewById(R.id.tvWelcome)
        tvWelcome.text = "$greeting, $fullName!"

        findViewById<TextView>(R.id.tvDate).text =
            SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

        findViewById<TextView>(R.id.tvAvatarInitial).text =
            fullName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "T"

        cardAttendance = findViewById(R.id.cardAttendance)
        cardTimetable = findViewById(R.id.cardTimetable)
        cardAttendanceReport = findViewById(R.id.cardAttendanceReport)
        val cardNoticesEvents = findViewById<View>(R.id.cardNoticesEvents)

        cardAttendance.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }
        cardTimetable.setOnClickListener {
            startActivity(Intent(this, TimeTableActivity::class.java))
        }
        cardAttendanceReport.setOnClickListener {
            showReportOptionsSheet()
        }
        cardNoticesEvents.setOnClickListener {
            startActivity(Intent(this, UpdatesListActivity::class.java))
        }

        animateEntrance()
    }

    private fun showReportOptionsSheet() {
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_report_options, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)

        sheetView.findViewById<View>(R.id.optionDaily).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, DailyAttendanceReportActivity::class.java))
        }
        sheetView.findViewById<View>(R.id.optionMonthly).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MonthlyAttendanceReportActivity::class.java))
        }
        dialog.show()
    }

    private fun getGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun animateEntrance() {
        val sections = listOf(
            findViewById<View>(R.id.headerSection),
            findViewById<View>(R.id.quickActionsSection)
        )
        sections.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 48f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setStartDelay(index * 90L)
                .setInterpolator(DecelerateInterpolator())
                .start()
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
