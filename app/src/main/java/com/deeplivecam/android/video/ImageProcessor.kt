package com.deeplivecam.android.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.deeplivecam.android.ml.FaceEnhancer
import com.deeplivecam.android.ml.FaceSwapProcessor
import com.deeplivecam.android.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Single image processing for face swap
 */
class ImageProcessor(private val context: Context) {
    
    private val faceSwapProcessor = FaceSwapProcessor.getInstance(context)
    private val faceEnhancer = FaceEnhancer.getInstance(context)
    
    /**
     * Process a single image with face swap
     */
    suspend fun processImage(
        inputUri: Uri,
        outputFile: File,
        enhanceFace: Boolean = false
    ): ProcessingResult = withContext(Dispatchers.IO) {
        try {
            // Load input image
            val inputStream = context.contentResolver.openInputStream(inputUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                return@withContext ProcessingResult.Error("Failed to load image")
            }
            
            // Scale if too large
            val scaledBitmap = BitmapUtils.scaleBitmap(bitmap, 2048, 2048)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            
            // Process face swap
            val swappedBitmap = faceSwapProcessor.processFaceSwap(scaledBitmap)
            
            // Optional enhancement
            val finalBitmap = if (enhanceFace) {
                val enhanced = faceEnhancer.enhance(swappedBitmap)
                if (enhanced != swappedBitmap) {
                    swappedBitmap.recycle()
                }
                enhanced
            } else {
                swappedBitmap
            }
            
            // Save to output file
            FileOutputStream(outputFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            // Cleanup
            scaledBitmap.recycle()
            if (finalBitmap != scaledBitmap) {
                finalBitmap.recycle()
            }
            
            Log.d(TAG, "Image processed successfully: ${outputFile.absolutePath}")
            ProcessingResult.Success(outputFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Image processing failed", e)
            ProcessingResult.Error(e.message ?: "Unknown error")
        }
    }
    
    sealed class ProcessingResult {
        data class Success(val outputFile: File) : ProcessingResult()
        data class Error(val message: String) : ProcessingResult()
    }
    
    companion object {
        private const val TAG = "ImageProcessor"
    }
}
