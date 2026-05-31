package com.example.ict2_project.models

import com.google.gson.annotations.SerializedName

data class Updates(
    @SerializedName(value = "Id", alternate = ["id"])
    val id: Int,

    @SerializedName(value = "Title", alternate = ["title"])
    val title: String,

    @SerializedName(value = "Description", alternate = ["description"])
    val description: String,

    @SerializedName(value = "CreatedAt", alternate = ["createdAt"])
    val createdAt: String? = null,

    // ⚠️ IMPORTANT: backend usually returns int (1/0), not string
    @SerializedName(value = "Status", alternate = ["status"])
    val status: Int? = null,

    @SerializedName(value = "FilePath", alternate = ["filePath"])
    val filePath: String? = null
)