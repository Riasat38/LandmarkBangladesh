package com.example.landmarkbangladesh.utils

import android.util.Log
import com.example.landmarkbangladesh.data.model.Landmark
import com.example.landmarkbangladesh.data.model.LandmarkResponse
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiTestUtil {
    
    fun testApiResponse(): List<Landmark> {
        return try {
            val url = URL("https://labs.anontech.info/cse489/t3/api.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d("ApiTestUtil", "Response Code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                Log.d("ApiTestUtil", "API Response: $response")
                
                // Try to parse the response
                parseApiResponse(response)
            } else {
                Log.e("ApiTestUtil", "HTTP Error: $responseCode")
                getTestLandmarks()
            }
        } catch (e: Exception) {
            Log.e("ApiTestUtil", "Error fetching data: ${e.message}")
            getTestLandmarks()
        }
    }
    
    private fun parseApiResponse(jsonString: String): List<Landmark> {
        return try {
            // Try parsing as array first
            val jsonArray = JSONArray(jsonString)
            val landmarks = mutableListOf<Landmark>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val landmark = parseLandmarkFromJson(jsonObject)
                if (landmark != null) {
                    landmarks.add(landmark)
                }
            }
            
            if (landmarks.isEmpty()) getTestLandmarks() else landmarks
        } catch (e: Exception) {
            Log.e("ApiTestUtil", "Error parsing JSON: ${e.message}")
            getTestLandmarks()
        }
    }
    
    private fun parseLandmarkFromJson(jsonObject: JSONObject): Landmark? {
        return try {
            Landmark(
                id = jsonObject.optInt("id", 0),
                title = jsonObject.optString("title", jsonObject.optString("name", "Unknown")),
                location = jsonObject.optString("location", jsonObject.optString("address", "Unknown Location")),
                description = jsonObject.optString("description", ""),
                image = jsonObject.optString("image", jsonObject.optString("image_url", jsonObject.optString("photo", ""))),
                latitude = jsonObject.optDouble("latitude", jsonObject.optDouble("lat", 0.0)),
                longitude = jsonObject.optDouble("longitude", jsonObject.optDouble("lng", 0.0)),
                category = jsonObject.optString("category", jsonObject.optString("type", "General"))
            )
        } catch (e: Exception) {
            Log.e("ApiTestUtil", "Error parsing landmark: ${e.message}")
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
            )
        )
    }
}
