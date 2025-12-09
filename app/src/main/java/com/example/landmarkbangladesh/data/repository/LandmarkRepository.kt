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

    //GET all landmarks
    suspend fun getLandmarks(): Result<List<Landmark>> = withContext(Dispatchers.IO) {
        try {
            Log.d("LandmarkRepository", " Fetching landmarks from API...")
            val response = apiService.getLandmarks()

            if (response.isSuccessful) {
                val landmarkResponses = response.body() ?: emptyList()
                Log.d("LandmarkRepository", " Successfully received ${landmarkResponses.size} landmarks from API")

                val landmarks = landmarkResponses.mapNotNull { it.toLandmark() }
                Log.d("LandmarkRepository", " Parsed ${landmarks.size} valid landmarks")

                landmarks.forEachIndexed { index, landmark ->
                    Log.d("LandmarkRepository", "  Landmark ${index + 1}: ${landmark.title} at (${landmark.latitude}, ${landmark.longitude})")
                }
                Result.success(landmarks)
            } else {
                Log.e("LandmarkRepository", " API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", " Network error: ${e.message}", e)
            Result.failure(e)
        }
    }

    //POST  new landmark
    suspend fun createLandmark(
        title: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri? = null,
        context: Context? = null
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("LandmarkRepository", " Creating new landmark: $title at ($latitude, $longitude)")
            Log.d("LandmarkRepository", " Image URI provided: ${imageUri != null}")

            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val latBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lonBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            var imagePart: MultipartBody.Part? = null
            if (imageUri != null && context != null) {
                Log.d("LandmarkRepository", " Preparing image for upload...")
                imagePart = prepareImagePart(imageUri, context)
                if (imagePart != null) {
                    Log.d("LandmarkRepository", " Image part prepared successfully")
                } else {
                    Log.w("LandmarkRepository", " Failed to prepare image part, continuing without image")
                }
            } else {
                Log.d("LandmarkRepository", " No image provided for this landmark")
            }

            Log.d("LandmarkRepository", " Sending POST request to API...")
            val response = apiService.createLandmark(titleBody, latBody, lonBody, imagePart)

            if (response.isSuccessful) {
                val result = response.body()
                Log.d("LandmarkRepository", " Successfully created landmark")
                Log.d("LandmarkRepository", " API Response: ${result?.status} - ${result?.message}")
                Result.success(result ?: ApiResponse(status = "success"))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("LandmarkRepository", " Failed to create landmark: HTTP ${response.code()}")
                Log.e("LandmarkRepository", " Error response: ${response.message()}")
                Log.e("LandmarkRepository", " Error body: $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", " Error creating landmark: ${e.message}", e)
            e.printStackTrace()
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
            Log.d("LandmarkRepository", "✏ Updating landmark ID: $id")

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
                Log.d("LandmarkRepository", " Successfully updated landmark: ${result?.message}")
                Result.success(result ?: ApiResponse(status = "success"))
            } else {
                Log.e("LandmarkRepository", " Failed to update landmark: ${response.code()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", " Error updating landmark: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Delete (DELETE): Remove a landmark permanently
    suspend fun deleteLandmark(id: Int): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("LandmarkRepository", "🗑️ Deleting landmark ID: $id")
            Log.d("LandmarkRepository", "📡 DELETE Request URL: https://labs.anontech.info/cse489/t3/api.php?id=$id")

            val response = apiService.deleteLandmark(id)

            if (response.isSuccessful) {
                val result = response.body()
                Log.d("LandmarkRepository", " Successfully deleted landmark: ${result?.message}")
                Result.success(result ?: ApiResponse(status = "success"))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("LandmarkRepository", " Failed to delete landmark")
                Log.e("LandmarkRepository", "   HTTP Status: ${response.code()}")
                Log.e("LandmarkRepository", "   Error Message: ${response.message()}")
                Log.e("LandmarkRepository", "   Error Body: $errorBody")
                Result.failure(Exception("Failed to delete: HTTP ${response.code()} - ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LandmarkRepository", " Error deleting landmark: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun prepareImagePart(imageUri: Uri, context: Context): MultipartBody.Part? {
        return try {
            Log.d("LandmarkRepository", "📷 Preparing image part from URI: $imageUri")

            val contentResolver = context.contentResolver

            // Get MIME type
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            Log.d("LandmarkRepository", "📄 Image MIME type: $mimeType")

            // Open input stream
            val inputStream = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("LandmarkRepository", " Cannot open input stream for URI: $imageUri")
                return null
            }

            // Create temporary file with proper extension
            val extension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("jpg") || mimeType.contains("jpeg") -> "jpg"
                else -> "jpg"
            }

            val fileName = "upload_image_${System.currentTimeMillis()}.$extension"
            val file = File(context.cacheDir, fileName)

            Log.d("LandmarkRepository", " Creating temp file: ${file.absolutePath}")

            // Copy stream to file
            file.outputStream().use { outputStream ->
                val bytesWritten = inputStream.copyTo(outputStream)
                Log.d("LandmarkRepository", " Wrote $bytesWritten bytes to temp file")
            }
            inputStream.close()

            // Verify file was created and has content
            if (!file.exists() || file.length() == 0L) {
                Log.e("LandmarkRepository", " Temp file creation failed or file is empty")
                return null
            }

            Log.d("LandmarkRepository", " Temp file created successfully: ${file.length()} bytes")

            // Create request body with proper MIME type
            val mediaType = when (extension) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                else -> "image/jpeg"
            }.toMediaTypeOrNull()

            val requestFile = file.asRequestBody(mediaType)
            val imagePart = MultipartBody.Part.createFormData("image", fileName, requestFile)

            Log.d("LandmarkRepository", " Image part prepared successfully for upload")
            return imagePart

        } catch (e: Exception) {
            Log.e("LandmarkRepository", " Error preparing image part: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    // Convert API response to domain model
    private fun LandmarkResponse.toLandmark(): Landmark? {
        return try {
            // Fix image URL - prepend base URL if it's a relative path
            val imageUrl = this.image?.let { img ->
                when {
                    img.startsWith("http://") || img.startsWith("https://") -> img
                    img.isNotEmpty() -> "https://labs.anontech.info/cse489/t3/$img"
                    else -> ""
                }
            } ?: ""

            Log.d("LandmarkRepository", " Image URL for '${this.title}': $imageUrl")

            Landmark(
                id = this.id ?: 0,
                title = this.title ?: "Unknown Landmark",
                location = "Lat: ${this.lat ?: 0.0}, Lon: ${this.lon ?: 0.0}",
                description = "Created: ${this.created_at ?: "Unknown"}",
                image = imageUrl,
                latitude = this.lat ?: 0.0,
                longitude = this.lon ?: 0.0,
            )
        } catch (e: Exception) {
            Log.e("LandmarkRepository", "Error converting LandmarkResponse: ${e.message}")
            null
        }
    }
}
