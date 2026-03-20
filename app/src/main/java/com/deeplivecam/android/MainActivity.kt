package com.deeplivecam.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.deeplivecam.android.ui.DeepLiveCamNavHost
import com.deeplivecam.android.ui.theme.DeepLiveCamTheme

class MainActivity : ComponentActivity() {

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        Log.d(TAG, "Permission results: $results")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "MainActivity onCreate - starting, SDK=${Build.VERSION.SDK_INT}")
        
        try {
            requestNeededPermissions()
            Log.d(TAG, "Permissions requested")
        } catch (e: Exception) {
            Log.e(TAG, "Permission request failed", e)
        }

        setContent {
            DeepLiveCamTheme {
                DeepLiveCamNavHost()
            }
        }
        
        Log.d(TAG, "MainActivity onCreate - completed")
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed += Manifest.permission.READ_MEDIA_IMAGES
            needed += Manifest.permission.READ_MEDIA_VIDEO
        } else {
            needed += Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: $missing")
            permissionsLauncher.launch(missing.toTypedArray())
        } else {
            Log.d(TAG, "All permissions already granted")
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
