package com.deeplivecam.android.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import java.io.ByteArrayOutputStream

object BitmapUtils {

    private const val TAG = "BitmapUtils"

    fun scaleBitmap(
        bitmap: Bitmap,
        maxWidth: Int = Constants.MAX_INPUT_WIDTH,
        maxHeight: Int = Constants.MAX_INPUT_HEIGHT,
        highQuality: Boolean = true
    ): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return bitmap
        }

        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        Log.d(TAG, "Scaling ${bitmap.width}x${bitmap.height} -> ${newWidth}x${newHeight}")
        
        return if (highQuality) {
            // High-quality scaling with anti-aliasing and filtering
            val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaledBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = false
            
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = Rect(0, 0, newWidth, newHeight)
            canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
            scaledBitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
    }

    fun safeRecycle(bitmap: Bitmap?) {
        try {
            if (bitmap != null && !bitmap.isRecycled) bitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Recycle error", e)
        }
    }

    fun compressBitmap(
        bitmap: Bitmap,
        quality: Int = (Constants.DEFAULT_QUALITY * 100).toInt()
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    fun createMutableCopy(bitmap: Bitmap): Bitmap {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun resizeToExact(bitmap: Bitmap, width: Int, height: Int, highQuality: Boolean = true): Bitmap {
        if (bitmap.width == width && bitmap.height == height) return bitmap
        
        return if (highQuality) {
            val resized = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resized)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
            resized
        } else {
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    }
}
