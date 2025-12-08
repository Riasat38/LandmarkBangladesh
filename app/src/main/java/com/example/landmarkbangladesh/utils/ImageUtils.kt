package com.example.landmarkbangladesh.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {

    private const val TARGET_WIDTH = 800
    private const val TARGET_HEIGHT = 600
    private const val JPEG_QUALITY = 85

    /**
     * Resize image to 800x600 while maintaining aspect ratio
     */
    suspend fun resizeImage(context: Context, imageUri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                Log.e("ImageUtils", "Failed to decode bitmap from URI")
                return@withContext null
            }

            // Get rotation angle from EXIF data
            val rotationAngle = getRotationAngle(context, imageUri)

            // Apply rotation if needed
            val rotatedBitmap = if (rotationAngle != 0f) {
                rotateBitmap(bitmap, rotationAngle)
            } else {
                bitmap
            }

            // Resize bitmap
            val resizedBitmap = resizeBitmap(rotatedBitmap, TARGET_WIDTH, TARGET_HEIGHT)

            // Save to temporary file
            val tempFile = File(context.cacheDir, "resized_landmark_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)

            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            outputStream.close()

            // Clean up bitmaps
            if (rotatedBitmap != bitmap) {
                rotatedBitmap.recycle()
            }
            bitmap.recycle()
            resizedBitmap.recycle()

            Log.d("ImageUtils", "Image resized successfully to ${resizedBitmap.width}x${resizedBitmap.height}")
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error resizing image", e)
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // Calculate scaling factor to maintain aspect ratio
        val scaleX = targetWidth.toFloat() / originalWidth
        val scaleY = targetHeight.toFloat() / originalHeight
        val scale = minOf(scaleX, scaleY)

        // Calculate new dimensions
        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()

        Log.d("ImageUtils", "Resizing from ${originalWidth}x${originalHeight} to ${newWidth}x${newHeight}")

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getRotationAngle(context: Context, imageUri: Uri): Float {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exifInterface = ExifInterface(inputStream!!)
            inputStream.close()

            when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: IOException) {
            Log.w("ImageUtils", "Could not get rotation angle", e)
            0f
        }
    }
}
