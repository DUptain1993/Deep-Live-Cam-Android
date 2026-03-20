package com.deeplivecam.android.utils

/**
 * Application-wide constants
 * Optimized for mid-range devices (Moto G 2025, 4-6GB RAM)
 */
object Constants {
    
    // Memory Management - Optimized for Moto G 2025
    const val MAX_MEMORY_MB = 4096  // 4GB device
    const val APP_MEMORY_LIMIT_MB = 384  // More conservative for stability
    const val MODEL_MEMORY_LIMIT_MB = 150  // Reduced for better stability
    const val BITMAP_MEMORY_LIMIT_MB = 80  // Reduced to prevent OOM
    
    // Image Processing - Balanced for Moto G 2025 (Snapdragon 4 Gen 2)
    const val MAX_INPUT_WIDTH = 1280  // Reduced from 1920 for better performance
    const val MAX_INPUT_HEIGHT = 1280
    const val PREVIEW_WIDTH = 960   // Reduced from 1280 for smoother preview
    const val PREVIEW_HEIGHT = 720  // Reduced from 960 for 16:9 ratio
    const val MODEL_INPUT_SIZE = 128  // Face swap model input
    const val ENHANCEMENT_INPUT_SIZE = 512  // Face enhancement (if enabled)
    
    // Camera Settings
    const val TARGET_FPS = 20  // Reduced from 24 for stability
    const val MIN_FPS = 12  // Reduced threshold
    const val CAMERA_ASPECT_RATIO = 16.0 / 9.0  // Modern aspect ratio
    
    // Video Processing
    const val VIDEO_BITRATE = 2_000_000  // 2 Mbps (reasonable quality)
    const val VIDEO_FRAME_RATE = 30
    const val VIDEO_I_FRAME_INTERVAL = 1
    
    // Model Files
    const val MODEL_DIR = "models"
    const val MODEL_FACE_SWAP = "inswapper_128_int8.tflite"  // Quantized INT8
    const val MODEL_FACE_DETECTOR = "face_detector_fp16.tflite"
    const val MODEL_FACE_ENHANCEMENT = "gfpgan_fp16.tflite"  // Optional
    
    // Storage
    const val OUTPUT_DIR = "DeepLiveCam"
    const val TEMP_DIR = "temp"
    const val CACHE_DIR = "cache"
    
    // Processing Options
    const val DEFAULT_QUALITY = 0.90f  // Balanced quality (reduced from 0.95)
    const val FACE_CONFIDENCE_THRESHOLD = 0.7f
    const val MAX_FACES = 3  // Reduced from 5 for performance
    
    // Performance Thresholds
    const val LOW_MEMORY_THRESHOLD_MB = 150  // Increased for earlier cleanup
    const val THERMAL_THROTTLE_TEMP = 38.0f  // Lower threshold for Moto G
    
    // Settings Keys
    const val PREF_NAME = "deep_live_cam_prefs"
    const val KEY_MANY_FACES = "many_faces"
    const val KEY_MOUTH_MASK = "mouth_mask"
    const val KEY_ENHANCEMENT = "face_enhancement"
    const val KEY_MIRROR = "mirror_camera"
    const val KEY_NSFW_FILTER = "nsfw_filter"
    const val KEY_EXECUTION_PROVIDER = "execution_provider"
    const val KEY_QUALITY = "output_quality"
    const val KEY_RESOLUTION = "resolution"
    const val KEY_TARGET_FPS = "target_fps"
    
    // Execution Providers
    const val EXEC_CPU = "cpu"
    const val EXEC_NNAPI = "nnapi"
    const val EXEC_GPU = "gpu_delegate"
}
