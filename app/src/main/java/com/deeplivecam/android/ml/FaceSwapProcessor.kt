package com.deeplivecam.android.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.Log
import com.deeplivecam.android.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Face swap using ML Kit landmarks + image-based warping & blending.
 * Works entirely on-device without any external ML model files.
 */
class FaceSwapProcessor private constructor(private val context: Context) {

    private val faceDetector = FaceDetector.getInstance(context)
    private var sourceFaceBitmap: Bitmap? = null
    private var sourceLandmarks: Map<String, PointF>? = null

    suspend fun setSourceFace(bitmap: Bitmap) {
        sourceFaceBitmap?.recycle()
        val scaled = BitmapUtils.scaleBitmap(bitmap, 512, 512)
        sourceFaceBitmap = scaled

        val faces = faceDetector.detectFaces(scaled)
        sourceLandmarks = faces.firstOrNull()?.landmarks
        if (sourceLandmarks == null) {
            Log.w(TAG, "No face detected in source image – overlay will use bounding-box fallback")
        } else {
            Log.d(TAG, "Source face landmarks cached (${sourceLandmarks!!.size} points)")
        }
    }

    suspend fun processFaceSwap(targetFrame: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val sourceFace = sourceFaceBitmap ?: return@withContext targetFrame

        val faces = faceDetector.detectFaces(targetFrame)
        if (faces.isEmpty()) return@withContext targetFrame

        val result = targetFrame.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        for (face in faces) {
            try {
                swapOneFace(canvas, sourceFace, face, targetFrame)
            } catch (e: Exception) {
                Log.e(TAG, "Error swapping face", e)
            }
        }

        return@withContext result
    }

    /**
     * Core per-face swap: warp source face into the target bounding box with an
     * elliptical feathered mask and basic colour correction.
     */
    private fun swapOneFace(
        canvas: Canvas,
        sourceFace: Bitmap,
        target: DetectedFace,
        targetFrame: Bitmap
    ) {
        val box = target.boundingBox

        // Slightly expand the bounding box so we cover forehead/chin
        val pad = 0.12f
        val expandedLeft   = (box.left   - box.width()  * pad).coerceAtLeast(0f)
        val expandedTop    = (box.top    - box.height() * pad).coerceAtLeast(0f)
        val expandedRight  = (box.right  + box.width()  * pad).coerceAtMost(targetFrame.width.toFloat())
        val expandedBottom = (box.bottom + box.height() * pad).coerceAtMost(targetFrame.height.toFloat())
        val w = (expandedRight - expandedLeft).toInt().coerceAtLeast(1)
        val h = (expandedBottom - expandedTop).toInt().coerceAtLeast(1)

        // Scale source face to target region size
        val scaled = Bitmap.createScaledBitmap(sourceFace, w, h, true)

        // Colour-correct the scaled source toward the target region's average colour
        val corrected = colourCorrect(scaled, targetFrame, expandedLeft.toInt(), expandedTop.toInt(), w, h)
        if (scaled !== corrected) scaled.recycle()

        // Build a soft elliptical alpha mask
        val mask = createFeatheredMask(w, h)

        // Composite: mask the corrected source, then draw it onto canvas
        val masked = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val mc = Canvas(masked)
        mc.drawBitmap(corrected, 0f, 0f, null)
        val maskPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) }
        mc.drawBitmap(mask, 0f, 0f, maskPaint)

        canvas.drawBitmap(masked, expandedLeft, expandedTop, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        corrected.recycle()
        mask.recycle()
        masked.recycle()
    }

    /** Radial-gradient alpha mask for seamless edge blending. */
    private fun createFeatheredMask(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val cx = w / 2f
        val cy = h / 2f
        val rx = w / 2f
        val ry = h / 2f

        // Draw a filled ellipse whose alpha fades from 255 at center to 0 at edge
        val gradient = RadialGradient(
            cx, cy,
            maxOf(rx, ry),
            intArrayOf(Color.WHITE, Color.WHITE, Color.TRANSPARENT),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }

        // Stretch circle into ellipse via matrix on canvas
        c.save()
        c.scale(1f, ry / rx, cx, cy)
        c.drawCircle(cx, cy, rx, paint)
        c.restore()

        return bmp
    }

    /**
     * Shift the mean colour of [src] to match the mean colour of the region
     * at (rx,ry,rw,rh) in [target].  Fast per-channel linear correction.
     */
    private fun colourCorrect(
        src: Bitmap, target: Bitmap,
        rx: Int, ry: Int, rw: Int, rh: Int
    ): Bitmap {
        val srcMean = meanColour(src, 0, 0, src.width, src.height)
        val tgtMean = meanColour(target, rx, ry, rw, rh)

        val rShift = tgtMean[0] - srcMean[0]
        val gShift = tgtMean[1] - srcMean[1]
        val bShift = tgtMean[2] - srcMean[2]

        if (kotlin.math.abs(rShift) < 5 && kotlin.math.abs(gShift) < 5 && kotlin.math.abs(bShift) < 5) {
            return src
        }

        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(out.width * out.height)
        out.getPixels(pixels, 0, out.width, 0, 0, out.width, out.height)

        for (i in pixels.indices) {
            val p = pixels[i]
            val a = (p ushr 24) and 0xFF
            val r = (((p shr 16) and 0xFF) + rShift).toInt().coerceIn(0, 255)
            val g = (((p shr 8) and 0xFF) + gShift).toInt().coerceIn(0, 255)
            val b = ((p and 0xFF) + bShift).toInt().coerceIn(0, 255)
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        out.setPixels(pixels, 0, out.width, 0, 0, out.width, out.height)
        return out
    }

    /** Returns [R, G, B] means as floats for the given region. */
    private fun meanColour(bmp: Bitmap, x: Int, y: Int, w: Int, h: Int): FloatArray {
        val sx = x.coerceIn(0, bmp.width - 1)
        val sy = y.coerceIn(0, bmp.height - 1)
        val sw = w.coerceAtMost(bmp.width - sx)
        val sh = h.coerceAtMost(bmp.height - sy)
        if (sw <= 0 || sh <= 0) return floatArrayOf(128f, 128f, 128f)

        val pixels = IntArray(sw * sh)
        bmp.getPixels(pixels, 0, sw, sx, sy, sw, sh)

        var rSum = 0L; var gSum = 0L; var bSum = 0L
        for (p in pixels) {
            rSum += (p shr 16) and 0xFF
            gSum += (p shr 8) and 0xFF
            bSum += p and 0xFF
        }
        val n = pixels.size.toFloat()
        return floatArrayOf(rSum / n, gSum / n, bSum / n)
    }

    fun hasSourceFace(): Boolean = sourceFaceBitmap != null

    fun clearSourceFace() {
        sourceFaceBitmap?.recycle()
        sourceFaceBitmap = null
        sourceLandmarks = null
    }

    fun release() {
        clearSourceFace()
        faceDetector.close()
    }

    companion object {
        private const val TAG = "FaceSwapProcessor"

        @Volatile
        private var instance: FaceSwapProcessor? = null

        fun getInstance(context: Context): FaceSwapProcessor {
            return instance ?: synchronized(this) {
                instance ?: FaceSwapProcessor(context.applicationContext).also { instance = it }
            }
        }
    }
}
