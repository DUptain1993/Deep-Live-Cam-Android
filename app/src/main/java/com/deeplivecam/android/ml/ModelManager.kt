package com.deeplivecam.android.ml

import android.content.Context
import android.util.Log
import com.deeplivecam.android.utils.Constants
import com.deeplivecam.android.utils.MemoryManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Manages ML model loading.
 * Models are optional — the app works without them using image-based face swap.
 */
class ModelManager private constructor(private val context: Context) {

    private val memoryManager = MemoryManager.getInstance(context)
    private val modelLock = ReentrantLock()

    @Volatile
    private var currentModel: LoadedModel? = null

    fun hasModel(modelType: ModelType): Boolean {
        return try {
            val assets = context.assets.list("$MODEL_DIR/") ?: emptyArray()
            modelType.fileName in assets
        } catch (e: Exception) {
            false
        }
    }

    fun loadModelFile(modelName: String): File? {
        return try {
            val modelFile = File(context.cacheDir, modelName)

            if (modelFile.exists()) {
                Log.d(TAG, "Model $modelName already cached")
                return modelFile
            }

            // Check the asset actually exists before attempting to read
            val assetList = context.assets.list(MODEL_DIR) ?: emptyArray()
            if (modelName !in assetList) {
                Log.w(TAG, "Model $modelName not found in assets – skipping")
                return null
            }

            val assetFd = context.assets.openFd("$MODEL_DIR/$modelName")
            val modelSizeMB = (assetFd.length / (1024 * 1024)).toInt()
            assetFd.close()

            if (!memoryManager.canLoadModel(modelSizeMB)) {
                memoryManager.aggressiveCleanup()
                if (!memoryManager.canLoadModel(modelSizeMB)) {
                    Log.e(TAG, "Insufficient memory for $modelName ($modelSizeMB MB)")
                    return null
                }
            }

            Log.d(TAG, "Copying $modelName from assets ($modelSizeMB MB)")
            context.assets.open("$MODEL_DIR/$modelName").use { input ->
                FileOutputStream(modelFile).use { output ->
                    input.copyTo(output)
                }
            }

            modelFile
        } catch (e: IOException) {
            Log.e(TAG, "Error loading model $modelName", e)
            null
        }
    }

    fun prepareModel(modelType: ModelType): File? = modelLock.withLock {
        currentModel?.let { loaded ->
            if (loaded.type != modelType) {
                Log.d(TAG, "Unloading ${loaded.type} to load $modelType")
                unloadCurrentModel()
            } else {
                return loaded.file
            }
        }

        val modelFile = loadModelFile(modelType.fileName)
        if (modelFile != null) {
            currentModel = LoadedModel(modelType, modelFile)
        }
        modelFile
    }

    fun unloadCurrentModel() = modelLock.withLock {
        currentModel?.let {
            Log.d(TAG, "Unloading model ${it.type}")
            currentModel = null
            memoryManager.forceGarbageCollection()
        }
    }

    fun getCurrentModelType(): ModelType? = currentModel?.type
    fun isModelLoaded(modelType: ModelType): Boolean = currentModel?.type == modelType

    fun clearCache() {
        unloadCurrentModel()
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".tflite") || file.name.endsWith(".onnx")) {
                file.delete()
            }
        }
    }

    companion object {
        private const val TAG = "ModelManager"
        private const val MODEL_DIR = "models"

        @Volatile
        private var instance: ModelManager? = null

        fun getInstance(context: Context): ModelManager {
            return instance ?: synchronized(this) {
                instance ?: ModelManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private data class LoadedModel(val type: ModelType, val file: File)
}

enum class ModelType(val fileName: String, val estimatedSizeMB: Int) {
    FACE_SWAP(Constants.MODEL_FACE_SWAP, 50),
    FACE_DETECTOR(Constants.MODEL_FACE_DETECTOR, 10),
    FACE_ENHANCEMENT(Constants.MODEL_FACE_ENHANCEMENT, 80);
}
