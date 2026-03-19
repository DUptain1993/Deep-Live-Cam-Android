package com.deeplivecam.android.utils

import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream

object BitmapUtils {

    private const val TAG = "BitmapUtils"

    fun scaleBitmap(
        bitmap: Bitmap,
        maxWidth: Int = Constants.MAX_INPUT_WIDTH,
        maxHeight: Int = Constants.MAX_INPUT_HEIGHT
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
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
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

    fun resizeToExact(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        if (bitmap.width == width && bitmap.height == height) return bitmap
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
