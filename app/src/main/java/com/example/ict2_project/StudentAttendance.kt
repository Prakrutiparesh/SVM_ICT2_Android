package com.example.ict2_project.models

import com.google.gson.annotations.SerializedName

// ==============================
// Student Model (for attendance list)
// ==============================

data class Student(
    @SerializedName(value = "StudentId", alternate = ["studentId"])
    val studentId: Int,

    @SerializedName(value = "FirstName", alternate = ["firstName"])
    val firstName: String,

    @SerializedName(value = "LastName", alternate = ["lastName"])
    val lastName: String,

    @SerializedName(value = "RollNo", alternate = ["rollNo"])
    val rollNo: Int,

    @SerializedName(value = "ClassName", alternate = ["className"])
    val className: String?,

    @SerializedName(value = "SectionName", alternate = ["sectionName"])
    val sectionName: String?,

    @SerializedName(value = "AdmissionNo", alternate = ["admissionNo"])
    val admissionNo: String?
) {
    val fullName: String
        get() = "${firstName} ${lastName}".trim()
}

// ==============================
// Request/Response for Attendance
// ==============================

data class BulkAttendanceRequest(
    @SerializedName("classId") val classId: Int,
    @SerializedName("sectionId") val sectionId: Int,
    @SerializedName("sessionId") val sessionId: Int,
    @SerializedName("attendanceDate") val attendanceDate: String,
    @SerializedName("attendances") val attendances: List<AttendanceItem>
)

data class AttendanceItem(
    @SerializedName("studentId") val studentId: Int,
    @SerializedName("status") val status: String
)

data class BulkAttendanceResponse(
    @SerializedName("message") val message: String,
    @SerializedName("alreadyExists") val alreadyExists: Boolean = false
)

data class AttendanceReport(
    @SerializedName(value = "Id", alternate = ["id"])
    val id: Int,

    @SerializedName(value = "StudentId", alternate = ["studentId"])
    val studentId: Int,

    @SerializedName(value = "StudentName", alternate = ["studentName"])
    val studentName: String,

    @SerializedName(value = "AttendanceDate", alternate = ["attendanceDate"])
    val attendanceDate: String,

    @SerializedName(value = "Status", alternate = ["status"])
    val status: String,

    @SerializedName(value = "ClassName", alternate = ["className"])
    val className: String?,

    @SerializedName(value = "SectionName", alternate = ["sectionName"])
    val sectionName: String?
)