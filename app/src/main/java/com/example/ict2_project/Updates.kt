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

    @SerializedName(value = "Status", alternate = ["status"])
    val status: Int? = null,

    @SerializedName(value = "FilePath", alternate = ["filePath"])
    val filePath: String? = null,

    @SerializedName(value = "Category", alternate = ["category"])
    val category: String? = null
)