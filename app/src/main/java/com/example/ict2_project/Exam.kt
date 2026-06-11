package com.example.ict2_project

import com.example.ict2_project.models.Subject
import com.google.gson.annotations.SerializedName

data class Exam(
    @SerializedName("examId")
    val examId: Int,

    @SerializedName("examName")
    val examName: String,

    @SerializedName("examType")
    val examType: String,

    @SerializedName("classId")
    val classId: Int,

    @SerializedName("sectionId")
    val sectionId: Int,

    @SerializedName("medium")
    val medium: String,

    @SerializedName("sessionId")
    val sessionId: Int,

    @SerializedName("isPublished")
    val isPublished: Boolean
)

data class TeacherSubject(
    @SerializedName("id")
    val id: Int,

    @SerializedName("staffId")
    val staffId: Int,

    @SerializedName("subjectId")
    val subjectId: Int,

    @SerializedName("classId")
    val classId: Int,

    @SerializedName("sessionId")
    val sessionId: Int
)

data class ExamSubject(
    @SerializedName("examSubjectId")
    val examSubjectId: Int,

    @SerializedName("examId")
    val examId: Int,

    @SerializedName("subjectId")
    val subjectId: Int,

    @SerializedName("totalMarks")
    val totalMarks: Int,

    @SerializedName("passingMarks")
    val passingMarks: Int,

    @SerializedName("subject")
    val subject: Subject? = null
) {
    // Computed property
    val subjectName: String
        get() = subject?.subjectName ?: ""
}

data class StudentMarksData(
    @SerializedName("studentId")
    val studentId: Int,

    @SerializedName("rollNo")
    val rollNo: Int,

    @SerializedName("studentName")
    val studentName: String,

    @SerializedName("grno")
    val grno: String,

    @SerializedName("marks")
    val marks: List<SubjectMark>
)

data class SubjectMark(
    @SerializedName("examSubjectId")
    val examSubjectId: Int,

    @SerializedName("obtainedMarks")
    val obtainedMarks: Float?
)

data class MarksDataResponse(
    @SerializedName("subjects")
    val subjects: List<ExamSubject>,

    @SerializedName("students")
    val students: List<StudentMarksData>
)

data class SaveMarksRequest(
    @SerializedName("examSubjectId")
    val examSubjectId: Int,

    @SerializedName("studentId")
    val studentId: Int,

    @SerializedName("obtainedMarks")
    val obtainedMarks: Float,

    @SerializedName("enteredBy")
    val enteredBy: Int
)