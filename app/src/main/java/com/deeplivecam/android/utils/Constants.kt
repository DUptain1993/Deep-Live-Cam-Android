package com.deeplivecam.android.utils

/**
 * Application-wide constants
 * Optimized for 4GB RAM devices
 */
object Constants {
    
    // Memory Management
    const val MAX_MEMORY_MB = 4096  // 4GB device
    const val APP_MEMORY_LIMIT_MB = 512  // Conservative app memory limit
    const val MODEL_MEMORY_LIMIT_MB = 200  // Max memory for ML models
    const val BITMAP_MEMORY_LIMIT_MB = 100  // Max memory for bitmaps
    
    // Image Processing (Optimized for 4GB RAM)
    const val MAX_INPUT_WIDTH = 720  // Reduced from 1080p
    const val MAX_INPUT_HEIGHT = 720
    const val PREVIEW_WIDTH = 640
    const val PREVIEW_HEIGHT = 480
    const val MODEL_INPUT_SIZE = 128  // Face swap model input
    const val ENHANCEMENT_INPUT_SIZE = 512  // Face enhancement (if enabled)
    
    // Camera Settings
    const val TARGET_FPS = 24  // Balanced for performance
    const val MIN_FPS = 15  // Minimum acceptable FPS
    const val CAMERA_ASPECT_RATIO = 4.0 / 3.0
    
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
    const val DEFAULT_QUALITY = 0.85f  // JPEG quality
    const val FACE_CONFIDENCE_THRESHOLD = 0.7f
    const val MAX_FACES = 5  // Limit for many-faces mode
    
    // Performance Thresholds
    const val LOW_MEMORY_THRESHOLD_MB = 100  // Trigger aggressive cleanup
    const val THERMAL_THROTTLE_TEMP = 40.0f  // Celsius
    
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
