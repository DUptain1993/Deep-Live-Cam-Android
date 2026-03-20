package com.deeplivecam.android

import android.app.Application
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeepLiveCamApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashHandler()
        Log.d(TAG, "DeepLiveCam Application started")
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val crashDir = File(getExternalFilesDir(null), "crash_logs")
                crashDir.mkdirs()
                val crashFile = File(crashDir, "crash_$timestamp.txt")
                PrintWriter(FileWriter(crashFile)).use { pw ->
                    pw.println("=== Deep Live Cam Crash Report ===")
                    pw.println("Time: $timestamp")
                    pw.println("Thread: ${thread.name}")
                    pw.println("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                    pw.println("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                    pw.println()
                    throwable.printStackTrace(pw)
                }
                Log.e(TAG, "Crash log written to ${crashFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write crash log", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "DeepLiveCamApp"
    }
}
