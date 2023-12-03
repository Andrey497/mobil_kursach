package com.example.serv1

import com.google.gson.annotations.SerializedName

data class ImageInfo(
    @SerializedName("accuracy ")
    val accuracy: String,
    val disease: String
)
