package com.deeplivecam.android.ml

import android.graphics.PointF
import android.graphics.RectF
import android.util.Log

/**
 * Face landmark mapping for better face alignment
 * Maps landmarks between source and target faces for improved quality
 */
object FaceLandmarkMapper {
    
    private const val TAG = "FaceLandmarkMapper"
    
    /**
     * Calculate transformation matrix between source and target landmarks
     * Returns scale, rotation, and translation
     */
    fun calculateTransform(
        sourceLandmarks: Map<String, PointF>,
        targetLandmarks: Map<String, PointF>
    ): FaceTransform? {
        // Need at least eyes and nose for basic alignment
        val sourceLeftEye = sourceLandmarks["left_eye"] ?: return null
        val sourceRightEye = sourceLandmarks["right_eye"] ?: return null
        val sourceNose = sourceLandmarks["nose"] ?: return null
        
        val targetLeftEye = targetLandmarks["left_eye"] ?: return null
        val targetRightEye = targetLandmarks["right_eye"] ?: return null
        val targetNose = targetLandmarks["nose"] ?: return null
        
        val sourceEyeDist = distance(sourceLeftEye, sourceRightEye)
        val targetEyeDist = distance(targetLeftEye, targetRightEye)
        
        if (sourceEyeDist < 1f) return null
        
        val scale = targetEyeDist / sourceEyeDist
        
        // Calculate rotation (based on eye line angle)
        val sourceAngle = angle(sourceLeftEye, sourceRightEye)
        val targetAngle = angle(targetLeftEye, targetRightEye)
        val rotation = targetAngle - sourceAngle
        
        // Calculate translation (center point between eyes)
        val sourceCenterX = (sourceLeftEye.x + sourceRightEye.x) / 2f
        val sourceCenterY = (sourceLeftEye.y + sourceRightEye.y) / 2f
        val targetCenterX = (targetLeftEye.x + targetRightEye.x) / 2f
        val targetCenterY = (targetLeftEye.y + targetRightEye.y) / 2f
        
        val translateX = targetCenterX - sourceCenterX * scale
        val translateY = targetCenterY - sourceCenterY * scale
        
        return FaceTransform(
            scale = scale,
            rotation = rotation,
            translateX = translateX,
            translateY = translateY
        )
    }
    
    /**
     * Get mouth region bounds from landmarks
     * Used for mouth masking feature
     */
    fun getMouthRegion(landmarks: Map<String, PointF>, boundingBox: RectF): RectF? {
        val mouthLeft = landmarks["mouth_left"]
        val mouthRight = landmarks["mouth_right"]
        val mouthBottom = landmarks["mouth_bottom"]
        
        if (mouthLeft == null || mouthRight == null) {
            // Fallback: use lower third of face
            return RectF(
                boundingBox.left + boundingBox.width() * 0.25f,
                boundingBox.top + boundingBox.height() * 0.65f,
                boundingBox.right - boundingBox.width() * 0.25f,
                boundingBox.bottom - boundingBox.height() * 0.1f
            )
        }
        
        // Create mouth bounding box with some padding
        val padding = boundingBox.width() * 0.1f
        val mouthTop = mouthLeft.y - padding
        val mouthBottomY = (mouthBottom?.y ?: mouthLeft.y) + padding
        
        return RectF(
            mouthLeft.x - padding,
            mouthTop,
            mouthRight.x + padding,
            mouthBottomY
        )
    }
    
    /**
     * Calculate distance between two points
     */
    private fun distance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Calculate angle between two points (in radians)
     */
    private fun angle(p1: PointF, p2: PointF): Float {
        return kotlin.math.atan2(p2.y - p1.y, p2.x - p1.x)
    }
    
    /**
     * Transform representing face alignment parameters
     */
    data class FaceTransform(
        val scale: Float,
        val rotation: Float,
        val translateX: Float,
        val translateY: Float
    ) {
        override fun toString(): String {
            return "FaceTransform(scale=%.2f, rotation=%.2f°, tx=%.1f, ty=%.1f)".format(
                scale, Math.toDegrees(rotation.toDouble()), translateX, translateY
            )
        }
    }
}
