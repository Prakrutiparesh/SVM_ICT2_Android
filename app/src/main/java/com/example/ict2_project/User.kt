package com.example.ict2_project.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("fullName")
    val fullName: String?,

    @SerializedName("groupId")  // ✅ Change from "group_id" to "groupId"
    val groupId: Int?,

    @SerializedName("profilePhoto")
    val profilePhoto: String?,
)