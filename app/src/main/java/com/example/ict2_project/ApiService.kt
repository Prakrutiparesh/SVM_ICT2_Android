package com.example.ict2_project.api

import com.example.ict2_project.ApiResponse
import com.example.ict2_project.Exam
import com.example.ict2_project.ExamSubject
import com.example.ict2_project.ForgotPasswordRequest
import com.example.ict2_project.MarksDataResponse
import com.example.ict2_project.ResetPasswordRequest
import com.example.ict2_project.SaveMarksRequest
import com.example.ict2_project.TeacherSubject
import com.example.ict2_project.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import com.google.gson.JsonElement

interface ApiService {

    // ---------- EXISTING METHODS (unchanged, return Call<T>) ----------
    @POST("api/Users/login")
    fun login(@Body request: LoginRequest): Call<User>

    @POST("api/Users/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ApiResponse>

    @POST("api/Users/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ApiResponse>

    @GET("api/Timetables")
    fun getTimetables(
        @Query("sessionId") sessionId: Int?,
        @Query("classId") classId: Int?,
        @Query("sectionId") sectionId: Int?
    ): Call<List<Timetable>>

    @GET("api/Timetables/SectionsByClass/{classId}")
    fun getSectionsByClass(@Path("classId") classId: Int): Call<List<Section>>

    @GET("api/Sessions")
    fun getSessions(): Call<List<Session>>

    @GET("api/Classes")
    fun getClasses(@Query("medium") medium: String? = null): Call<List<Class>>

    @GET("api/Timetables/GetTeacherMapping")
    fun getTeacherMapping(
        @Query("sessionId") sessionId: Int,
        @Query("classId") classId: Int
    ): Call<Map<Int, Staff>>

    @GET("api/Classes")
    fun getClassesByMedium(@Query("medium") medium: String): Call<List<Class>>

    // ---------- NEW ATTENDANCE METHODS (suspend for AttendanceActivity) ----------
    @GET("api/StudentAttendances/students")
    suspend fun getStudentsForAttendance(
        @Query("sessionId") sessionId: Int,
        @Query("medium") medium: String?,
        @Query("classId") classId: Int?,
        @Query("sectionId") sectionId: Int?
    ): List<Student>

    @POST("api/StudentAttendances/bulk")
    suspend fun submitAttendance(@Body request: BulkAttendanceRequest): Response<BulkAttendanceResponse>

    // Inside ApiService.kt

    @GET("api/StudentAttendances/advanced-report")
    suspend fun getDailyAttendanceReport(
        @Query("sessionId") sessionId: Int,
        @Query("medium") medium: String? = null,
        @Query("classId") classId: Int? = null,
        @Query("sectionId") sectionId: Int? = null,
        @Query("date") date: String   // yyyy-MM-dd
    ): Response<DailyReportResponse>

    @GET("api/StudentAttendances/monthly-report")
    suspend fun getMonthlyAttendanceReportRawJson(
        @Query("sessionId") sessionId: Int,
        @Query("medium") medium: String? = null,
        @Query("classId") classId: Int? = null,
        @Query("sectionId") sectionId: Int? = null,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<JsonElement>

    @GET("api/Updates/active")
    suspend fun getActiveUpdates(): Response<List<Updates>>

    @GET("api/Exams/teacher-exams")
    suspend fun getTeacherExams(
        @Query("staffId") staffId: Int,
        @Query("publishedOnly") publishedOnly: Boolean = true
    ): List<Exam>

    @GET("api/TeacherSubjects")
    suspend fun getTeacherSubjects(): List<TeacherSubject>

    @GET("api/Exams/subjects/{examId}")  // ✅ Fixed - added "api/"
    suspend fun getExamSubjects(@Path("examId") examId: Int): List<ExamSubject>

    @GET("api/Exams/marks-data")
    suspend fun getMarksData(
        @Query("examId") examId: Int,
        @Query("classId") classId: Int,
        @Query("sectionId") sectionId: Int
    ): MarksDataResponse

    @POST("api/Exams/marks/save")
    suspend fun saveMarks(@Body marks: List<SaveMarksRequest>): Response<Unit>


    // Add this to ApiService.kt
    @GET("api/Staffs/ByUserId/{userId}")
    suspend fun getStaffByUserId(@Path("userId") userId: Int): Staff


    @GET("api/Exams")  // Already correct
    suspend fun getExamsByFilters(
        @Query("sessionId") sessionId: Int,
        @Query("medium") medium: String,
        @Query("classId") classId: Int,
        @Query("sectionId") sectionId: Int,
        @Query("publishedOnly") publishedOnly: Boolean = true
    ): List<Exam>

    // Change endpoint to match your controller
    @GET("api/TeacherSubjects/by-staff")  // ← Make sure this matches
    suspend fun getTeacherSubjectsByStaff(
        @Query("staffId") staffId: Int,
        @Query("classId") classId: Int?,
        @Query("sessionId") sessionId: Int?
    ): List<TeacherSubject>


}