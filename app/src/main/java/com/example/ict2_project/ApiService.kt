package com.example.ict2_project.api

import com.example.ict2_project.ApiResponse
import com.example.ict2_project.ForgotPasswordRequest
import com.example.ict2_project.ResetPasswordRequest
import com.example.ict2_project.models.LoginRequest
import com.example.ict2_project.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/Users/login")
    fun login(@Body request: LoginRequest): Call<User>

    @POST("api/Users/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ApiResponse>

    @POST("api/Users/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ApiResponse>
}