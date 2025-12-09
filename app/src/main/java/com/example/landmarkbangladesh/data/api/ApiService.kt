package com.example.landmarkbangladesh.data.api

import com.example.landmarkbangladesh.data.model.ApiResponse
import com.example.landmarkbangladesh.data.model.LandmarkResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {


    @GET("api.php")
    suspend fun getLandmarks(): Response<List<LandmarkResponse>>


    @Multipart
    @POST("api.php")
    suspend fun createLandmark(
        @Part("title") title: RequestBody,
        @Part("lat") latitude: RequestBody,
        @Part("lon") longitude: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse>

    @Multipart
    @PUT("api.php")
    suspend fun updateLandmark(
        @Part("id") id: RequestBody,
        @Part("title") title: RequestBody?,
        @Part("lat") latitude: RequestBody?,
        @Part("lon") longitude: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse>


    @HTTP(method = "DELETE", path = "api.php", hasBody = true)
    @FormUrlEncoded
    suspend fun deleteLandmark(
        @Field("id") id: Int
    ): Response<ApiResponse>
}
