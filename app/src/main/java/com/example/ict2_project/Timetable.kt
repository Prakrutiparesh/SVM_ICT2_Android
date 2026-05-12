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

data class Subject(
    @SerializedName("subjectId") val subjectId: Int,
    @SerializedName("subjectName") val subjectName: String
)

data class Staff(
    @SerializedName("staffId") val staffId: Int,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class Session(
    @SerializedName("sessionId") val sessionId: Int,
    @SerializedName("sessionName") val sessionName: String
)

data class Class(
    @SerializedName("classId") val classId: Int,
    @SerializedName("className") val className: String,
    @SerializedName("medium") val medium: String? = null
)

data class Section(
    @SerializedName("sectionId") val sectionId: Int,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("classId") val classId: Int
)