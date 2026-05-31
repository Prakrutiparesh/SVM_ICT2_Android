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
    @SerializedName(value = "StaffId", alternate = ["staffId"])
    val staffId: Int,

    @SerializedName(value = "FirstName", alternate = ["firstName"])
    val firstName: String?,

    @SerializedName(value = "LastName", alternate = ["lastName"])
    val lastName: String?
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
    val sessionName: String
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