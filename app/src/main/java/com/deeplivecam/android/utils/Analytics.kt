package com.deeplivecam.android.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Analytics and event logging
 * Basic event tracking for understanding app usage
 * Privacy-focused: all data stored locally, no external tracking
 */
class Analytics private constructor(private val context: Context) {
    
    private val logFile = File(context.filesDir, "analytics.log")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    
    /**
     * Log an event
     */
    suspend fun logEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        withContext(Dispatchers.IO) {
            try {
                val event = JSONObject().apply {
                    put("timestamp", dateFormat.format(Date()))
                    put("event", eventName)
                    put("properties", JSONObject(properties))
                }
                
                // Append to log file
                logFile.appendText("${event}\n")
                
                // Log to console in debug builds
                Log.d(TAG, "Event: $eventName, Properties: $properties")
                
                // Rotate log file if too large (keep last 100 entries)
                rotateLogIfNeeded()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log event", e)
            }
        }
    }
    
    /**
     * Log app launch
     */
    suspend fun logAppLaunch() {
        logEvent("app_launch")
    }
    
    /**
     * Log face swap processing
     */
    suspend fun logFaceSwap(processingTimeMs: Long, faceCount: Int) {
        logEvent("face_swap", mapOf(
            "processing_time_ms" to processingTimeMs,
            "face_count" to faceCount
        ))
    }
    
    /**
     * Log video processing
     */
    suspend fun logVideoProcessing(
        durationMs: Long,
        frameCount: Int,
        success: Boolean
    ) {
        logEvent("video_processing", mapOf(
            "duration_ms" to durationMs,
            "frame_count" to frameCount,
            "success" to success
        ))
    }
    
    /**
     * Log image processing
     */
    suspend fun logImageProcessing(success: Boolean, enhancementEnabled: Boolean) {
        logEvent("image_processing", mapOf(
            "success" to success,
            "enhancement" to enhancementEnabled
        ))
    }
    
    /**
     * Log settings change
     */
    suspend fun logSettingsChange(setting: String, value: Any) {
        logEvent("settings_change", mapOf(
            "setting" to setting,
            "value" to value.toString()
        ))
    }
    
    /**
     * Log error
     */
    suspend fun logError(errorType: String, message: String) {
        logEvent("error", mapOf(
            "error_type" to errorType,
            "message" to message
        ))
    }
    
    /**
     * Log thermal event
     */
    suspend fun logThermalEvent(state: String, temperature: Float) {
        logEvent("thermal_event", mapOf(
            "state" to state,
            "temperature" to temperature
        ))
    }
    
    /**
     * Get analytics summary
     */
    suspend fun getSummary(): AnalyticsSummary = withContext(Dispatchers.IO) {
        try {
            if (!logFile.exists()) {
                return@withContext AnalyticsSummary()
            }
            
            val lines = logFile.readLines()
            var faceSwapCount = 0
            var videoProcessingCount = 0
            var imageProcessingCount = 0
            var errorCount = 0
            
            for (line in lines) {
                try {
                    val json = JSONObject(line)
                    when (json.getString("event")) {
                        "face_swap" -> faceSwapCount++
                        "video_processing" -> videoProcessingCount++
                        "image_processing" -> imageProcessingCount++
                        "error" -> errorCount++
                    }
                } catch (e: Exception) {
                    // Skip malformed lines
                }
            }
            
            AnalyticsSummary(
                totalEvents = lines.size,
                faceSwapCount = faceSwapCount,
                videoProcessingCount = videoProcessingCount,
                imageProcessingCount = imageProcessingCount,
                errorCount = errorCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get analytics summary", e)
            AnalyticsSummary()
        }
    }
    
    /**
     * Clear all analytics data
     */
    suspend fun clear() {
        withContext(Dispatchers.IO) {
            try {
                logFile.delete()
                Log.d(TAG, "Analytics data cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear analytics", e)
            }
        }
    }
    
    /**
     * Rotate log file if it gets too large
     */
    private fun rotateLogIfNeeded() {
        try {
            if (!logFile.exists()) return
            
            val lines = logFile.readLines()
            if (lines.size > MAX_LOG_ENTRIES) {
                // Keep only the last MAX_LOG_ENTRIES
                val recentLines = lines.takeLast(MAX_LOG_ENTRIES)
                logFile.writeText(recentLines.joinToString("\n") + "\n")
                Log.d(TAG, "Log file rotated, kept ${recentLines.size} entries")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate log file", e)
        }
    }
    
    data class AnalyticsSummary(
        val totalEvents: Int = 0,
        val faceSwapCount: Int = 0,
        val videoProcessingCount: Int = 0,
        val imageProcessingCount: Int = 0,
        val errorCount: Int = 0
    )
    
    companion object {
        private const val TAG = "Analytics"
        private const val MAX_LOG_ENTRIES = 1000
        
        @Volatile
        private var instance: Analytics? = null
        
        fun getInstance(context: Context): Analytics {
            return instance ?: synchronized(this) {
                instance ?: Analytics(context.applicationContext).also { instance = it }
            }
        }
    }
}
