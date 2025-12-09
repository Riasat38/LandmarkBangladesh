package com.example.landmarkbangladesh.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)

object LocationUtils {

    /**
     * Get current GPS coordinates
     */
    suspend fun getCurrentLocation(context: Context): LocationCoordinates? {
        // Check permissions
        if (!hasLocationPermission(context)) {
            Log.w("LocationUtils", "Location permission not granted")
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if GPS is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w("LocationUtils", "GPS is not enabled")
            return null
        }

        return try {
            suspendCancellableCoroutine { continuation ->
                try {
                    // Try to get last known location first
                    val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastKnownLocation != null && isLocationRecent(lastKnownLocation)) {
                        Log.d("LocationUtils", "Using last known location: ${lastKnownLocation.latitude}, ${lastKnownLocation.longitude}")
                        continuation.resume(
                            LocationCoordinates(
                                lastKnownLocation.latitude,
                                lastKnownLocation.longitude,
                                lastKnownLocation.accuracy
                            )
                        )
                        return@suspendCancellableCoroutine
                    }

                    // Request fresh location update
                    val locationListener = object : android.location.LocationListener {
                        override fun onLocationChanged(location: Location) {
                            Log.d("LocationUtils", "Got fresh location: ${location.latitude}, ${location.longitude}")
                            locationManager.removeUpdates(this)
                            continuation.resume(
                                LocationCoordinates(
                                    location.latitude,
                                    location.longitude,
                                    location.accuracy
                                )
                            )
                        }

                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                    }

                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L, // minimum time interval
                        0f, // minimum distance
                        locationListener
                    )

                    // Set up cancellation
                    continuation.invokeOnCancellation {
                        locationManager.removeUpdates(locationListener)
                    }

                    // Timeout after 10 seconds
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        locationManager.removeUpdates(locationListener)
                        if (continuation.isActive) {
                            Log.w("LocationUtils", "Location request timed out")
                            continuation.resume(null)
                        }
                    }, 10000)

                } catch (e: SecurityException) {
                    Log.e("LocationUtils", "Security exception getting location", e)
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "Error getting current location", e)
            null
        }
    }

    private fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationRecent(location: Location): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        return locationAge < 2 * 60 * 1000 // 2 minutes
    }

    /**
     * Get required location permissions
     */
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}