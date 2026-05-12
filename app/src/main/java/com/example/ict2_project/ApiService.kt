package com.example.ict2_project.api

import com.example.ict2_project.ApiResponse
import com.example.ict2_project.ForgotPasswordRequest
import com.example.ict2_project.ResetPasswordRequest
import com.example.ict2_project.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

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

}