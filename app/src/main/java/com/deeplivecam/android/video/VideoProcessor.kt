package com.deeplivecam.android.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import com.deeplivecam.android.ml.FaceSwapProcessor
import com.deeplivecam.android.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

/**
 * Video processing for face swap on video files
 * Handles frame-by-frame processing with MediaCodec
 */
class VideoProcessor(private val context: Context) {
    
    private val faceSwapProcessor = FaceSwapProcessor.getInstance(context)
    
    /**
     * Process a video file with face swap
     * Returns flow of progress (0.0 to 1.0)
     */
    fun processVideo(
        inputUri: Uri,
        outputFile: File,
        onProgress: (Float) -> Unit = {}
    ): Flow<ProcessingResult> = flow {
        try {
            emit(ProcessingResult.Progress(0f, "Starting video processing..."))
            
            val extractor = MediaExtractor()
            extractor.setDataSource(context, inputUri, null)
            
            // Find video track
            var videoTrackIndex = -1
            var videoFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/")) {
                    videoTrackIndex = i
                    videoFormat = format
                    break
                }
            }
            
            if (videoTrackIndex < 0 || videoFormat == null) {
                emit(ProcessingResult.Error("No video track found"))
                return@flow
            }
            
            extractor.selectTrack(videoTrackIndex)
            
            // Get video properties
            val width = videoFormat.getInteger(MediaFormat.KEY_WIDTH)
            val height = videoFormat.getInteger(MediaFormat.KEY_HEIGHT)
            val duration = videoFormat.getLong(MediaFormat.KEY_DURATION)
            val frameRate = if (videoFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
            } else {
                30 // default
            }
            
            Log.d(TAG, "Video: ${width}x${height}, ${frameRate}fps, duration=${duration}µs")
            
            // Setup decoder
            val mime = videoFormat.getString(MediaFormat.KEY_MIME)!!
            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(videoFormat, null, null, 0)
            decoder.start()
            
            // Setup encoder
            val outputFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constants.VIDEO_BITRATE)
            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constants.VIDEO_I_FRAME_INTERVAL)
            
            val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
            
            // Setup muxer
            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var muxerVideoTrack = -1
            var muxerStarted = false
            
            // Process frames
            var inputDone = false
            var outputDone = false
            var frameCount = 0
            val totalFrames = (duration * frameRate / 1_000_000).toInt()
            
            val bufferInfo = MediaCodec.BufferInfo()
            
            while (!outputDone) {
                // Feed input
                if (!inputDone) {
                    val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferIndex)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            val presentationTimeUs = extractor.sampleTime
                            decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }
                
                // Get decoded frame
                val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                if (outputBufferIndex >= 0) {
                    // TODO: Extract frame as bitmap, process with face swap, encode
                    // For now, just pass through
                    decoder.releaseOutputBuffer(outputBufferIndex, false)
                    
                    frameCount++
                    val progress = frameCount.toFloat() / totalFrames
                    onProgress(progress)
                    emit(ProcessingResult.Progress(progress, "Processing frame $frameCount/$totalFrames"))
                    
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                }
            }
            
            // Cleanup
            decoder.stop()
            decoder.release()
            encoder.stop()
            encoder.release()
            if (muxerStarted) muxer.stop()
            muxer.release()
            extractor.release()
            
            emit(ProcessingResult.Success(outputFile))
            
        } catch (e: Exception) {
            Log.e(TAG, "Video processing failed", e)
            emit(ProcessingResult.Error(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Get video metadata
     */
    suspend fun getVideoMetadata(uri: Uri): VideoMetadata? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull() ?: 30
            
            retriever.release()
            
            VideoMetadata(width, height, duration, frameRate)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get video metadata", e)
            null
        }
    }
    
    sealed class ProcessingResult {
        data class Progress(val progress: Float, val message: String) : ProcessingResult()
        data class Success(val outputFile: File) : ProcessingResult()
        data class Error(val message: String) : ProcessingResult()
    }
    
    data class VideoMetadata(
        val width: Int,
        val height: Int,
        val durationMs: Long,
        val frameRate: Int
    )
    
    companion object {
        private const val TAG = "VideoProcessor"
        private const val TIMEOUT_US = 10000L
    }
}
