package com.example.ict2_project.models

import com.google.gson.annotations.SerializedName

data class Timetable(
    @SerializedName("timetableId") val timetableId: Int,
    @SerializedName("sessionId") val sessionId: Int,
    @SerializedName("classId") val classId: Int,
    @SerializedName("sectionId") val sectionId: Int,
    @SerializedName("dayName") val dayName: String,
    @SerializedName("lectureNo") val lectureNo: Int,
    @SerializedName("subjectId") val subjectId: Int?,
    @SerializedName("staffId") val staffId: Int?,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("isBreak") val isBreak: Boolean?,

    @SerializedName("subject") val subject: Subject?,
    @SerializedName("staff") var staff: Staff?,
    @SerializedName("session") val session: Session?,
    @SerializedName("class") val classObj: Class?,
    @SerializedName("section") val section: Section?
)

data class Staff(
    @SerializedName("staffId")
    val staffId: Int,

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("designation")
    val designation: String?,

    @SerializedName("qualification")
    val qualification: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("phone")
    val phone: String?
) {
    val fullName: String
        get() = "${firstName ?: ""} ${lastName ?: ""}".trim()
}

data class Subject(
    @SerializedName(value = "SubjectId", alternate = ["subjectId"])
    val subjectId: Int,

    @SerializedName(value = "SubjectName", alternate = ["subjectName"])
    val subjectName: String
)

data class Session(
    @SerializedName(value = "SessionId", alternate = ["sessionId"])
    val sessionId: Int,

    @SerializedName(value = "SessionName", alternate = ["sessionName"])
    val sessionName: String,

    @SerializedName(value = "StartYear", alternate = ["startYear"])
    val startYear: Int = 0,

    @SerializedName(value = "EndYear", alternate = ["endYear"])
    val endYear: Int = 0,

    @SerializedName(value = "IsActive", alternate = ["isActive"])
    val isActive: Int = 1
)

data class Class(
    @SerializedName("ClassId", alternate = ["classId"])
    val classId: Int,

    @SerializedName("ClassName", alternate = ["className"])
    val className: String,

    @SerializedName("Medium", alternate = ["medium"])
    val medium: String? = null,

    @SerializedName("SessionId", alternate = ["sessionId"])
    val sessionId: Int? = null
)

data class Section(
    @SerializedName(value = "SectionId", alternate = ["sectionId"])
    val sectionId: Int,

    @SerializedName(value = "SectionName", alternate = ["sectionName"])
    val sectionName: String,

    @SerializedName(value = "ClassId", alternate = ["classId"])
    val classId: Int
)