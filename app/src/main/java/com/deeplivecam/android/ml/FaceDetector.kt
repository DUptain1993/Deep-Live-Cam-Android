package com.deeplivecam.android.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.tasks.await

class FaceDetector private constructor(context: Context) {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)  // Changed to ACCURATE
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.10f)  // Lowered from 0.15 to detect smaller faces
            .enableTracking()
            .build()

        FaceDetection.getClient(options)
    }

    suspend fun detectFaces(bitmap: Bitmap): List<DetectedFace> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val faces = detector.process(image).await()

            Log.d(TAG, "Detected ${faces.size} faces")

            faces.map { face ->
                DetectedFace(
                    boundingBox = face.boundingBox,
                    confidence = 1.0f,
                    trackingId = face.trackingId,
                    landmarks = extractLandmarks(face),
                    headEulerAngleY = face.headEulerAngleY,
                    headEulerAngleZ = face.headEulerAngleZ
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            emptyList()
        }
    }

    private fun extractLandmarks(face: Face): Map<String, PointF> {
        val landmarks = mutableMapOf<String, PointF>()

        face.getLandmark(FaceLandmark.LEFT_EYE)?.let { landmarks["left_eye"] = it.position }
        face.getLandmark(FaceLandmark.RIGHT_EYE)?.let { landmarks["right_eye"] = it.position }
        face.getLandmark(FaceLandmark.NOSE_BASE)?.let { landmarks["nose"] = it.position }
        face.getLandmark(FaceLandmark.MOUTH_LEFT)?.let { landmarks["mouth_left"] = it.position }
        face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.let { landmarks["mouth_right"] = it.position }
        face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.let { landmarks["mouth_bottom"] = it.position }
        face.getLandmark(FaceLandmark.LEFT_EAR)?.let { landmarks["left_ear"] = it.position }
        face.getLandmark(FaceLandmark.RIGHT_EAR)?.let { landmarks["right_ear"] = it.position }
        face.getLandmark(FaceLandmark.LEFT_CHEEK)?.let { landmarks["left_cheek"] = it.position }
        face.getLandmark(FaceLandmark.RIGHT_CHEEK)?.let { landmarks["right_cheek"] = it.position }

        return landmarks
    }

    fun close() {
        detector.close()
        Log.d(TAG, "Face detector closed")
    }

    companion object {
        private const val TAG = "FaceDetector"

        @Volatile
        private var instance: FaceDetector? = null

        fun getInstance(context: Context): FaceDetector {
            return instance ?: synchronized(this) {
                instance ?: FaceDetector(context.applicationContext).also { instance = it }
            }
        }
    }
}

data class DetectedFace(
    val boundingBox: Rect,
    val confidence: Float,
    val trackingId: Int? = null,
    val landmarks: Map<String, PointF>,
    val headEulerAngleY: Float,
    val headEulerAngleZ: Float
) {
    fun getCenter(): PointF {
        return PointF(
            boundingBox.exactCenterX(),
            boundingBox.exactCenterY()
        )
    }

    fun isFrontal(angleThreshold: Float = 30f): Boolean {
        return kotlin.math.abs(headEulerAngleY) < angleThreshold &&
               kotlin.math.abs(headEulerAngleZ) < angleThreshold
    }
}
