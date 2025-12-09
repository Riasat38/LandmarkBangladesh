package com.example.landmarkbangladesh.data.model

// API Response models based on the REST API specification
data class ApiResponse(
    val status: String? = null,
    val message: String? = null,
    val data: List<LandmarkResponse>? = null
)

data class LandmarkResponse(
    val id: Int? = null,
    val title: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val image: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

// Request models for API operations
data class CreateLandmarkRequest(
    val title: String,
    val lat: Double,
    val lon: Double

)

data class UpdateLandmarkRequest(
    val id: Int,
    val title: String? = null,
    val lat: Double? = null,
    val lon: Double? = null

)

data class DeleteLandmarkRequest(
    val id: Int
)
