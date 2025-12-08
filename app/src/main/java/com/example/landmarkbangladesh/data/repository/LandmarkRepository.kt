package com.example.landmarkbangladesh.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.landmarkbangladesh.data.api.ApiService
import com.example.landmarkbangladesh.data.model.ApiResponse
import com.example.landmarkbangladesh.data.model.Landmark
import com.example.landmarkbangladesh.data.model.LandmarkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class LandmarkRepository {

    private val apiService: ApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://labs.anontech.info/cse489/t3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    // Read (GET): Retrieve all landmarks
    suspend fun getLandmarks(): Result<List<Landmark>> = withContext(Dispatchers.IO) {
        try {
            Log.d("LandmarkRepository", "🌐 Fetching landmarks from API...")
            val response = apiService.getLandmarks()

            if (response.isSuccessful) {
                val landmarkResponses = response.body() ?: emptyList()
                Log.d("LandmarkRepository", "✅ Successfully received ${landmarkResponses.size} landmarks from API")

                val landmarks = landmarkResponses.mapNotNull { it.toLandmark() }
                Log.d("LandmarkRepository", "📊 Parsed ${landmarks.size} valid landmarks")

                if (landmarks.isNotEmpty()) {
                    landmarks.forEachIndexed { index, landmark ->
                        Log.d("LandmarkRepository", "  Landmark ${index + 1}: ${landmark.title} at (${landmark.latitude}, ${landmark.longitude})")
                    }
                    Result.success(landmarks)
                } else {
                    Log.w("LandmarkRepository", "⚠️ No valid landmarks parsed, using fallback data")
                    Result.success(getTestLandmarks())
                }
            } else {
                Log.e("LandmarkRepository", "❌ API error: ${response.code()} - ${response.message()}")
                Log.d("LandmarkRepository", "Using fallback test data")
                Result.success(getTestLandmarks())
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", "❌ Network error: ${e.message}", e)
            Log.d("LandmarkRepository", "Using fallback test data")
            Result.success(getTestLandmarks())
        }
    }

    // Create (POST): Submit a new landmark
    suspend fun createLandmark(
        title: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri? = null,
        context: Context? = null
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("LandmarkRepository", "🆕 Creating new landmark: $title at ($latitude, $longitude)")

            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val latBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lonBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            var imagePart: MultipartBody.Part? = null
            if (imageUri != null && context != null) {
                imagePart = prepareImagePart(imageUri, context)
            }

            val response = apiService.createLandmark(titleBody, latBody, lonBody, imagePart)

            if (response.isSuccessful) {
                val result = response.body()
                Log.d("LandmarkRepository", "✅ Successfully created landmark: ${result?.message}")
                Result.success(result ?: ApiResponse(status = "success"))
            } else {
                Log.e("LandmarkRepository", "❌ Failed to create landmark: ${response.code()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", "❌ Error creating landmark: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Update (PUT): Modify an existing landmark
    suspend fun updateLandmark(
        id: Int,
        title: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        imageUri: Uri? = null,
        context: Context? = null
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("LandmarkRepository", "✏️ Updating landmark ID: $id")

            val idBody = id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val titleBody = title?.toRequestBody("text/plain".toMediaTypeOrNull())
            val latBody = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lonBody = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            var imagePart: MultipartBody.Part? = null
            if (imageUri != null && context != null) {
                imagePart = prepareImagePart(imageUri, context)
            }

            val response = apiService.updateLandmark(idBody, titleBody, latBody, lonBody, imagePart)

            if (response.isSuccessful) {
                val result = response.body()
                Log.d("LandmarkRepository", "✅ Successfully updated landmark: ${result?.message}")
                Result.success(result ?: ApiResponse(status = "success"))
            } else {
                Log.e("LandmarkRepository", "❌ Failed to update landmark: ${response.code()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", "❌ Error updating landmark: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Delete (DELETE): Remove a landmark permanently
    suspend fun deleteLandmark(id: Int): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("LandmarkRepository", "🗑️ Deleting landmark ID: $id")

            val response = apiService.deleteLandmark(id)

            if (response.isSuccessful) {
                val result = response.body()
                Log.d("LandmarkRepository", "✅ Successfully deleted landmark: ${result?.message}")
                Result.success(result ?: ApiResponse(status = "success"))
            } else {
                Log.e("LandmarkRepository", "❌ Failed to delete landmark: ${response.code()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", "❌ Error deleting landmark: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun prepareImagePart(imageUri: Uri, context: Context): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        } catch (e: Exception) {
            Log.e("LandmarkRepository", "Error preparing image part: ${e.message}")
            null
        }
    }

    // Convert API response to domain model
    private fun LandmarkResponse.toLandmark(): Landmark? {
        return try {
            Landmark(
                id = this.id ?: 0,
                title = this.title ?: "Unknown Landmark",
                location = "Lat: ${this.lat ?: 0.0}, Lon: ${this.lon ?: 0.0}",
                description = "Created: ${this.created_at ?: "Unknown"}",
                image = this.image ?: "",
                latitude = this.lat ?: 0.0,
                longitude = this.lon ?: 0.0,
                category = "Bangladesh Landmark"
            )
        } catch (e: Exception) {
            Log.e("LandmarkRepository", "Error converting LandmarkResponse: ${e.message}")
            null
        }
    }

    private fun getTestLandmarks(): List<Landmark> {
        return listOf(
            Landmark(
                id = 1,
                title = "Sundarbans Mangrove Forest",
                location = "Khulna Division, Bangladesh",
                description = "The largest mangrove forest in the world and a UNESCO World Heritage Site.",
                image = "https://via.placeholder.com/400x300/4CAF50/FFFFFF?text=Sundarbans",
                latitude = 21.9497,
                longitude = 89.1833,
                category = "Natural Heritage"
            ),
            Landmark(
                id = 2,
                title = "Cox's Bazar Beach",
                location = "Cox's Bazar, Chittagong Division",
                description = "The world's longest natural sea beach stretching 120 kilometers.",
                image = "https://via.placeholder.com/400x300/2196F3/FFFFFF?text=Cox's+Bazar",
                latitude = 21.4272,
                longitude = 92.0058,
                category = "Beach"
            ),
            Landmark(
                id = 3,
                title = "Lalbagh Fort",
                location = "Old Dhaka, Dhaka",
                description = "A 17th-century Mughal fort complex built during the reign of Emperor Aurangzeb.",
                image = "https://via.placeholder.com/400x300/FF9800/FFFFFF?text=Lalbagh+Fort",
                latitude = 23.7197,
                longitude = 90.3875,
                category = "Historical"
            ),
            Landmark(
                id = 4,
                title = "Shat Gombuj Mosque",
                location = "Bagerhat, Khulna Division",
                description = "A UNESCO World Heritage Site featuring 15th-century mosque architecture.",
                image = "https://via.placeholder.com/400x300/9C27B0/FFFFFF?text=Shat+Gombuj",
                latitude = 22.6833,
                longitude = 89.7833,
                category = "Religious"
            ),
            Landmark(
                id = 5,
                title = "Paharpur Buddhist Vihara",
                location = "Naogaon District, Rajshahi Division",
                description = "Ancient Buddhist monastery ruins dating back to the 8th century.",
                image = "https://via.placeholder.com/400x300/795548/FFFFFF?text=Paharpur",
                latitude = 25.0342,
                longitude = 88.9769,
                category = "Archaeological"
            ),
            Landmark(
                id = 6,
                title = "Bandarban Hill Tracts",
                location = "Bandarban, Chittagong Hill Tracts",
                description = "Scenic hill district with beautiful landscapes and tribal culture.",
                image = "https://via.placeholder.com/400x300/8BC34A/FFFFFF?text=Bandarban",
                latitude = 22.1953,
                longitude = 92.2207,
                category = "Natural"
            ),
            Landmark(
                id = 7,
                title = "Ahsan Manzil",
                location = "Old Dhaka, Dhaka",
                description = "The Pink Palace - former residential palace of the Nawab of Dhaka.",
                image = "https://via.placeholder.com/400x300/E91E63/FFFFFF?text=Ahsan+Manzil",
                latitude = 23.7085,
                longitude = 90.4068,
                category = "Historical"
            ),
            Landmark(
                id = 8,
                title = "Saint Martin's Island",
                location = "Cox's Bazar District",
                description = "The only coral island of Bangladesh with crystal clear blue water.",
                image = "https://via.placeholder.com/400x300/00BCD4/FFFFFF?text=Saint+Martin's",
                latitude = 20.5983,
                longitude = 92.3250,
                category = "Island"
            )
        )
    }
}
