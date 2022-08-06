package com.example.deviceinfoassignment

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.http.*

interface API_Interface {

    @Headers("Content-Type: application/json")
    @POST("/api/status")
    fun requestLogin(@Body requestModel: RequestModel): Call<RequestModel>
}