package com.example.ict2_project.models

import com.google.gson.annotations.SerializedName

// ==============================
// Student Model (for attendance list)
// ==============================

data class Student(
    @SerializedName("studentId") val studentId: Int,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("rollNo") val rollNo: Int,
    @SerializedName("className") val className: String?,
    @SerializedName("sectionName") val sectionName: String?,
    @SerializedName("admissionNo") val admissionNo: String?
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

// ==============================
// Request/Response for Attendance
// ==============================

data class BulkAttendanceRequest(
    @SerializedName("classId") val classId: Int,
    @SerializedName("sectionId") val sectionId: Int,
    @SerializedName("sessionId") val sessionId: Int,
    @SerializedName("attendanceDate") val attendanceDate: String, // "yyyy-MM-dd"
    @SerializedName("attendances") val attendances: List<AttendanceItem>
)

data class AttendanceItem(
    @SerializedName("studentId") val studentId: Int,
    @SerializedName("status") val status: String  // "Present", "Absent", "Late"
)

data class BulkAttendanceResponse(
    @SerializedName("message") val message: String,
    @SerializedName("alreadyExists") val alreadyExists: Boolean = false
)

data class AttendanceReport(
    @SerializedName("id") val id: Int,
    @SerializedName("studentId") val studentId: Int,
    @SerializedName("studentName") val studentName: String,
    @SerializedName("attendanceDate") val attendanceDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("className") val className: String?,
    @SerializedName("sectionName") val sectionName: String?
)