package com.example.deviceinfoassignment

import com.google.gson.annotations.SerializedName

data class RequestModel(
    val device: String,
    @SerializedName("internet-connected")
    val internet_connected: String,
    val charging: String,
    val battery: String,
    val location: String

)
