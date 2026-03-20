package com.deeplivecam.android.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.deeplivecam.android.utils.BitmapUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var currentLensFacing: Int = CameraSelector.LENS_FACING_FRONT

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState: StateFlow<CameraState> = _cameraState

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var frameProcessor: ((Bitmap) -> Bitmap?)? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_FRONT,
        onFrameProcessed: (Bitmap) -> Bitmap?
    ) {
        try {
            _cameraState.value = CameraState.Starting
            currentLensFacing = lensFacing
            frameProcessor = onFrameProcessed

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                    cameraProvider?.unbindAll()

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    preview = Preview.Builder()
                        .setTargetResolution(
                            android.util.Size(Constants.PREVIEW_WIDTH, Constants.PREVIEW_HEIGHT)
                        )
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetResolution(
                            android.util.Size(Constants.PREVIEW_WIDTH, Constants.PREVIEW_HEIGHT)
                        )
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                processFrame(imageProxy)
                            }
                        }

                    camera = cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )

                    _cameraState.value = CameraState.Running
                    Log.d(TAG, "Camera started successfully (facing=$lensFacing)")
                } catch (e: Exception) {
                    Log.e(TAG, "Camera bind failed", e)
                    _cameraState.value = CameraState.Error(e.message ?: "Camera bind failed")
                }
            }, androidx.core.content.ContextCompat.getMainExecutor(context))

        } catch (e: Exception) {
            Log.e(TAG, "Camera initialization failed", e)
            _cameraState.value = CameraState.Error(e.message ?: "Unknown error")
        }
    }

    private fun processFrame(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap()
            // Use higher resolution from Constants for better quality
            val scaledBitmap = BitmapUtils.scaleBitmap(
                bitmap, 
                Constants.PREVIEW_WIDTH, 
                Constants.PREVIEW_HEIGHT,
                highQuality = true
            )
            if (scaledBitmap !== bitmap) {
                bitmap.recycle()
            }

            val processedBitmap = frameProcessor?.invoke(scaledBitmap)

            if (processedBitmap != null) {
                _cameraState.value = CameraState.FrameProcessed(processedBitmap)
            }

            if (processedBitmap !== scaledBitmap) {
                scaledBitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Frame processing error", e)
        } finally {
            imageProxy.close()
        }
    }

    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            camera = null
            preview = null
            imageAnalyzer = null
            _cameraState.value = CameraState.Idle
            Log.d(TAG, "Camera stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
        }
    }

    fun switchCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val newFacing = if (currentLensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }

        stopCamera()
        val processor = frameProcessor ?: return
        startCamera(lifecycleOwner, previewView, newFacing, processor)
    }

    fun release() {
        stopCamera()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraManager"
    }
}

sealed class CameraState {
    data object Idle : CameraState()
    data object Starting : CameraState()
    data object Running : CameraState()
    data class FrameProcessed(val bitmap: Bitmap) : CameraState()
    data class Error(val message: String) : CameraState()
}
