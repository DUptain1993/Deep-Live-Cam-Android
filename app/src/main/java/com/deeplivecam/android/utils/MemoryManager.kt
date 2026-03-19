package com.deeplivecam.android.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import java.lang.ref.WeakReference

class MemoryManager private constructor(context: Context) {

    private val contextRef = WeakReference(context.applicationContext)
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val runtime = Runtime.getRuntime()

    data class MemoryInfo(
        val totalDeviceMemory: Long,
        val availableMemory: Long,
        val lowMemory: Boolean,
        val threshold: Long,
        val nativeHeapUsed: Long,
        val javaHeapUsed: Long,
        val javaHeapAvailable: Long
    )

    fun getMemoryInfo(): MemoryInfo {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        return MemoryInfo(
            totalDeviceMemory = memInfo.totalMem / (1024 * 1024),
            availableMemory = memInfo.availMem / (1024 * 1024),
            lowMemory = memInfo.lowMemory,
            threshold = memInfo.threshold / (1024 * 1024),
            nativeHeapUsed = Debug.getNativeHeapAllocatedSize() / (1024 * 1024),
            javaHeapUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
            javaHeapAvailable = runtime.maxMemory() / (1024 * 1024)
        )
    }

    fun isLowMemory(): Boolean {
        val info = getMemoryInfo()
        return info.lowMemory ||
               info.availableMemory < Constants.LOW_MEMORY_THRESHOLD_MB ||
               info.javaHeapUsed > (info.javaHeapAvailable * 0.8)
    }

    fun getAvailableProcessingMemory(): Long {
        val info = getMemoryInfo()
        return minOf(info.availableMemory, info.javaHeapAvailable - info.javaHeapUsed)
    }

    fun canLoadModel(modelSizeMB: Int): Boolean {
        return getAvailableProcessingMemory() > modelSizeMB + 50
    }

    fun forceGarbageCollection() {
        Log.d(TAG, "Forcing GC")
        System.gc()
        System.runFinalization()
    }

    fun logMemoryStatus() {
        val info = getMemoryInfo()
        Log.d(TAG, "Memory: device=${info.totalDeviceMemory}MB avail=${info.availableMemory}MB " +
            "java=${info.javaHeapUsed}/${info.javaHeapAvailable}MB native=${info.nativeHeapUsed}MB " +
            "low=${info.lowMemory}")
    }

    fun aggressiveCleanup() {
        Log.w(TAG, "Aggressive memory cleanup")
        forceGarbageCollection()
        logMemoryStatus()
    }

    companion object {
        private const val TAG = "MemoryManager"

        @Volatile
        private var instance: MemoryManager? = null

        fun getInstance(context: Context): MemoryManager {
            return instance ?: synchronized(this) {
                instance ?: MemoryManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
