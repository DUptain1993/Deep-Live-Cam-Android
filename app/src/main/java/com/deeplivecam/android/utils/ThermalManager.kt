package com.deeplivecam.android.utils

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Thermal management for preventing device overheating
 * Monitors device temperature and provides warnings
 */
class ThermalManager private constructor(private val context: Context) {
    
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val _thermalState = MutableStateFlow(ThermalState.NORMAL)
    val thermalState: StateFlow<ThermalState> = _thermalState
    
    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    /**
     * Start monitoring device temperature
     */
    fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        
        monitoringJob = scope.launch {
            while (true) {
                val temp = getCurrentTemperature()
                val state = when {
                    temp >= CRITICAL_TEMP -> ThermalState.CRITICAL
                    temp >= WARNING_TEMP -> ThermalState.WARNING
                    temp >= THROTTLE_TEMP -> ThermalState.THROTTLING
                    else -> ThermalState.NORMAL
                }
                
                if (state != _thermalState.value) {
                    _thermalState.value = state
                    Log.d(TAG, "Thermal state changed: $state (${temp}°C)")
                }
                
                delay(MONITORING_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _thermalState.value = ThermalState.NORMAL
    }
    
    fun release() {
        stopMonitoring()
        scope.cancel()
    }
    
    /**
     * Get current device temperature in Celsius
     * Uses multiple methods depending on Android version and device
     */
    private fun getCurrentTemperature(): Float {
        // Method 1: Use PowerManager thermal status (Android 9+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val status = powerManager.currentThermalStatus
            return when (status) {
                PowerManager.THERMAL_STATUS_NONE -> 30f
                PowerManager.THERMAL_STATUS_LIGHT -> 35f
                PowerManager.THERMAL_STATUS_MODERATE -> 40f
                PowerManager.THERMAL_STATUS_SEVERE -> 45f
                PowerManager.THERMAL_STATUS_CRITICAL -> 50f
                PowerManager.THERMAL_STATUS_EMERGENCY -> 55f
                PowerManager.THERMAL_STATUS_SHUTDOWN -> 60f
                else -> 30f
            }
        }
        
        // Method 2: Read from thermal zone files (requires root or specific permissions)
        try {
            val tempFiles = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/devices/platform/omap/omap_temp_sensor.0/temperature"
            )
            
            for (path in tempFiles) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val temp = file.readText().trim().toFloatOrNull()
                    if (temp != null) {
                        // Temperature is usually in millidegrees
                        return if (temp > 1000) temp / 1000f else temp
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read thermal zone: ${e.message}")
        }
        
        // Method 3: Estimate from battery temperature
        try {
            val batteryTemp = getBatteryTemperature()
            if (batteryTemp > 0) {
                // Battery temp is typically 5-10°C lower than CPU
                return batteryTemp + 5f
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get battery temperature: ${e.message}")
        }
        
        // Default: assume normal
        return 30f
    }
    
    /**
     * Get battery temperature from system
     */
    private fun getBatteryTemperature(): Float {
        try {
            val intent = context.registerReceiver(
                null,
                android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            )
            val temp = intent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            if (temp > 0) {
                return temp / 10f // Temperature is in tenths of degree
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get battery temperature: ${e.message}")
        }
        return -1f
    }
    
    /**
     * Check if device is throttling due to thermal conditions
     */
    fun isThrottling(): Boolean {
        return _thermalState.value in listOf(
            ThermalState.THROTTLING,
            ThermalState.WARNING,
            ThermalState.CRITICAL
        )
    }
    
    /**
     * Get recommended action based on thermal state
     */
    fun getRecommendedAction(): String {
        return when (_thermalState.value) {
            ThermalState.NORMAL -> "Processing normally"
            ThermalState.THROTTLING -> "Device warming up, reducing quality recommended"
            ThermalState.WARNING -> "Device hot, consider reducing FPS or resolution"
            ThermalState.CRITICAL -> "Device overheating! Stop processing immediately"
        }
    }
    
    enum class ThermalState {
        NORMAL,      // < 40°C
        THROTTLING,  // 40-45°C
        WARNING,     // 45-50°C
        CRITICAL     // > 50°C
    }
    
    companion object {
        private const val TAG = "ThermalManager"
        private const val MONITORING_INTERVAL_MS = 2000L // Check every 2 seconds
        
        // Temperature thresholds in Celsius
        private const val THROTTLE_TEMP = 40f
        private const val WARNING_TEMP = 45f
        private const val CRITICAL_TEMP = 50f
        
        @Volatile
        private var instance: ThermalManager? = null
        
        fun getInstance(context: Context): ThermalManager {
            return instance ?: synchronized(this) {
                instance ?: ThermalManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
