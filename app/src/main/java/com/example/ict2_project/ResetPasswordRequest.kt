package com.example.ict2_project

data class ResetPasswordRequest(val identifier: String, val otp: String, val newPassword: String)
