package com.example.ict2_project.models

import com.google.gson.annotations.SerializedName
import java.util.*

// ---------- Monthly Attendance Models ----------
data class MonthlyReportResponse(
    val students: List<MonthlyReportStudent>?,
    val dates: List<String>?,          // Changed from List<Date> to List<String>
    val totalDays: Int
)

data class MonthlyReportStudent(
    val rollNo: Int,
    val studentName: String,
    val gender: String,
    val dailyStatus: List<DailyStatus>,
    val present: Int,
    val absent: Int,
    val totalDays: Int,
    val percentage: Double
)

data class DailyStatus(
    val date: String,   // Already String – fine
    val status: String  // "P" or "A"
)

// ---------- Existing models (unchanged) ----------
data class DailyReportResponse(
    val students: List<DailyReportStudent>?,
    val totals: AttendanceTotals?,
    @SerializedName("isAttendanceMarked") val isAttendanceMarked: Boolean = false
)

data class DailyReportStudent(
    val studentId: Int,
    val fullName: String,
    val rollNo: Int,
    val gender: String,
    val status: String,
    val attendanceId: Int
) : java.io.Serializable

data class AttendanceTotals(
    val totalPresent: Int,
    val totalAbsent: Int,
    val girlsPresent: Int,
    val girlsAbsent: Int,
    val boysPresent: Int,
    val boysAbsent: Int
) : java.io.Serializable