    package com.example.ict2_project.activities   // adjust package as needed

    import android.content.Intent
    import android.os.Bundle
    import androidx.appcompat.app.AppCompatActivity
    import com.example.ict2_project.DailyAttendanceReportActivity
    import com.example.ict2_project.MonthlyAttendanceReportActivity
    import com.example.ict2_project.R
    import com.google.android.material.card.MaterialCardView

    class AttendanceReportActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_attendance_report)

            val cardDaily = findViewById<MaterialCardView>(R.id.cardDailyReport)
            val cardMonthly = findViewById<MaterialCardView>(R.id.cardMonthlyReport)

            cardDaily.setOnClickListener {
                startActivity(Intent(this, DailyAttendanceReportActivity::class.java))
            }

            cardMonthly.setOnClickListener {
                startActivity(Intent(this, MonthlyAttendanceReportActivity::class.java))
            }
        }
    }