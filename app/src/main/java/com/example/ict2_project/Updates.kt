package com.example.ict2_project.models

import com.google.gson.annotations.SerializedName

data class Updates(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("filePath") val filePath: String? = null   // Image or PDF URL

)