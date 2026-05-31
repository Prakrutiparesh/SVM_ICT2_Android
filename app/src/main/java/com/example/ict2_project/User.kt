package com.example.ict2_project.models
import com.google.gson.annotations.SerializedName

data class User(
    val userId: Int,
    val username: String,
    val email: String?,
    @SerializedName("FullName")
    val fullName: String?,

    @SerializedName("groupId")
    val groupId: Int?,

    val profilePhoto: String?,
)