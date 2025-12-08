package com.example.landmarkbangladesh.debug

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

object ApiDebugger {

    suspend fun testApi(): String = withContext(Dispatchers.IO) {
        try {
            Log.d("ApiDebugger", "Testing API endpoint: https://labs.anontech.info/cse489/t3/api.php")

            val url = URL("https://labs.anontech.info/cse489/t3/api.php")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "LandmarkBangladesh-Debug")
            }

            val responseCode = connection.responseCode
            val contentType = connection.contentType
            val contentLength = connection.contentLength

            Log.d("ApiDebugger", "Response Code: $responseCode")
            Log.d("ApiDebugger", "Content Type: $contentType")
            Log.d("ApiDebugger", "Content Length: $contentLength")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                Log.d("ApiDebugger", "Raw API Response:")
                Log.d("ApiDebugger", response)
                Log.d("ApiDebugger", "Response Length: ${response.length}")

                // Try to analyze the response format
                analyzeResponse(response)

                response
            } else {
                val errorMessage = "HTTP Error: $responseCode"
                Log.e("ApiDebugger", errorMessage)

                // Try to read error response
                try {
                    val errorReader = BufferedReader(InputStreamReader(connection.errorStream))
                    val errorResponse = errorReader.readText()
                    errorReader.close()
                    Log.e("ApiDebugger", "Error Response: $errorResponse")
                } catch (e: Exception) {
                    Log.e("ApiDebugger", "Could not read error response: ${e.message}")
                }

                errorMessage
            }
        } catch (e: Exception) {
            val errorMessage = "Network Error: ${e.message}"
            Log.e("ApiDebugger", errorMessage, e)
            errorMessage
        }
    }

    private fun analyzeResponse(response: String) {
        if (response.isBlank()) {
            Log.w("ApiDebugger", "Response is empty!")
            return
        }

        val trimmed = response.trim()
        Log.d("ApiDebugger", "Response starts with: ${trimmed.take(50)}")
        Log.d("ApiDebugger", "Response ends with: ${trimmed.takeLast(50)}")

        when {
            trimmed.startsWith("[") -> {
                Log.d("ApiDebugger", "Response appears to be a JSON array")
                try {
                    val array = JSONArray(trimmed)
                    Log.d("ApiDebugger", "Array has ${array.length()} items")
                    if (array.length() > 0) {
                        Log.d("ApiDebugger", "First item: ${array.getJSONObject(0)}")
                    }
                } catch (e: Exception) {
                    Log.e("ApiDebugger", "Failed to parse as JSON array: ${e.message}")
                }
            }
            trimmed.startsWith("{") -> {
                Log.d("ApiDebugger", "Response appears to be a JSON object")
                try {
                    val obj = JSONObject(trimmed)
                    Log.d("ApiDebugger", "Object keys: ${obj.keys()}")
                } catch (e: Exception) {
                    Log.e("ApiDebugger", "Failed to parse as JSON object: ${e.message}")
                }
            }
            else -> {
                Log.d("ApiDebugger", "Response appears to be plain text or HTML")
                Log.d("ApiDebugger", "Full response: $trimmed")
            }
        }
    }
}
