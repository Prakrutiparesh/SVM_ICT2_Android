package com.example.ict2_project.models

data class User(
    val userId: Int,
    val username: String,
    val email: String?,
    val fullName: String?,
    val groupId: Int?,
    val profilePhoto: String?,
)