package com.deeplivecam.android.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Optional TFLite inference engine.  The app works without any .tflite models
 * (falling back to the image-based face swap in [FaceSwapProcessor]).
 *
 * When models are present in assets/models/ this engine can be used for
 * higher-quality inference.
 */
class TFLiteInferenceEngine private constructor(
    private val context: Context
) {

    private val modelManager = ModelManager.getInstance(context)
    private var interpreter: org.tensorflow.lite.Interpreter? = null
    private var currentDelegate: Delegate? = null

    fun initializeModel(
        modelType: ModelType,
        useGpu: Boolean = true,
        useNnapi: Boolean = true
    ): Boolean {
        try {
            val modelFile = modelManager.prepareModel(modelType)
            if (modelFile == null) {
                Log.w(TAG, "Model file not available – running without ${modelType.name}")
                return false
            }

            val options = org.tensorflow.lite.Interpreter.Options().apply {
                numThreads = 2

                if (useGpu) {
                    try {
                        val compat = org.tensorflow.lite.gpu.CompatibilityList()
                        if (compat.isDelegateSupportedOnThisDevice) {
                            addDelegate(org.tensorflow.lite.gpu.GpuDelegate())
                            currentDelegate = Delegate.GPU
                            Log.d(TAG, "GPU delegate enabled")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "GPU delegate unavailable", e)
                    }
                }

                if (currentDelegate == null && useNnapi) {
                    try {
                        addDelegate(org.tensorflow.lite.nnapi.NnApiDelegate())
                        currentDelegate = Delegate.NNAPI
                        Log.d(TAG, "NNAPI delegate enabled")
                    } catch (e: Exception) {
                        Log.w(TAG, "NNAPI unavailable", e)
                    }
                }

                if (currentDelegate == null) {
                    currentDelegate = Delegate.CPU
                    Log.d(TAG, "Using CPU inference")
                }
            }

            interpreter = org.tensorflow.lite.Interpreter(modelFile, options)
            Log.d(TAG, "Model ${modelType.name} initialised with ${currentDelegate?.name}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error initialising model", e)
            return false
        }
    }

    fun isReady(): Boolean = interpreter != null

    fun runInference(input: Bitmap, outputSize: Int): FloatArray? {
        val interp = interpreter ?: return null

        return try {
            val inputBuffer = bitmapToByteBuffer(input)

            val outputShape = interp.getOutputTensor(0).shape()
            val outputBuffer = ByteBuffer.allocateDirect(
                outputShape.fold(4) { acc, i -> acc * i }
            ).apply { order(ByteOrder.nativeOrder()) }

            val t0 = System.currentTimeMillis()
            interp.run(inputBuffer, outputBuffer)
            Log.v(TAG, "Inference ${System.currentTimeMillis() - t0}ms")

            outputBuffer.rewind()
            val output = FloatArray(outputBuffer.remaining() / 4)
            outputBuffer.asFloatBuffer().get(output)
            output
        } catch (e: Exception) {
            Log.e(TAG, "Inference error", e)
            null
        }
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val interp = interpreter!!
        val inputShape = interp.getInputTensor(0).shape()
        val inputSize = inputShape[1]

        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val resized = if (bitmap.width != inputSize || bitmap.height != inputSize) {
            Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        } else {
            bitmap
        }

        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
        }

        if (resized !== bitmap) resized.recycle()
        return byteBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        currentDelegate = null
        modelManager.unloadCurrentModel()
        Log.d(TAG, "Inference engine closed")
    }

    fun getCurrentDelegate(): Delegate? = currentDelegate

    companion object {
        private const val TAG = "TFLiteEngine"

        @Volatile
        private var instance: TFLiteInferenceEngine? = null

        fun getInstance(context: Context): TFLiteInferenceEngine {
            return instance ?: synchronized(this) {
                instance ?: TFLiteInferenceEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    enum class Delegate { CPU, GPU, NNAPI }
}
