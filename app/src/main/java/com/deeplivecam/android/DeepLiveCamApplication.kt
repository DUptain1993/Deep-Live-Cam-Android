package com.deeplivecam.android

import android.app.Application
import android.util.Log

class DeepLiveCamApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DeepLiveCam Application started")
    }

    companion object {
        private const val TAG = "DeepLiveCamApp"
    }
}
