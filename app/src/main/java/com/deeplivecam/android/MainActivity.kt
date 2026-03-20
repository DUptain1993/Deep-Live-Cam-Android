package com.deeplivecam.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.deeplivecam.android.ui.DeepLiveCamNavHost
import com.deeplivecam.android.ui.MainScreen
import com.deeplivecam.android.ui.theme.DeepLiveCamTheme

class MainActivity : ComponentActivity() {

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* don't force-close on denial; the UI will explain */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "MainActivity onCreate - starting")
        
        try {
            requestNeededPermissions()
            Log.d(TAG, "Permissions requested")

            setContent {
                DeepLiveCamTheme {
                    try {
                        DeepLiveCamNavHost()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in DeepLiveCamNavHost", e)
                        // Fallback to error display
                        Box(
                            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("Error: ${e.message}")
                        }
                    }
                }
            }
            
            Log.d(TAG, "MainActivity onCreate - completed")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in MainActivity.onCreate", e)
            throw e
        }
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
            permissionsLauncher.launch(missing.toTypedArray())
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
