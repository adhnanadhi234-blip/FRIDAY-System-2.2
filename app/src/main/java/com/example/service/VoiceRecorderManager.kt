package com.example.service

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import com.example.model.AudioRecordItem
import java.io.File
import java.io.IOException
import java.util.UUID

class VoiceRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentOutputFile: File? = null
    private var isRecording = false
    private var isPaused = false

    private val recordingsDir: File by lazy {
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun isCurrentlyRecording(): Boolean = isRecording
    fun isCurrentlyPaused(): Boolean = isPaused

    fun startRecording(): Boolean {
        try {
            stopPlayback()
            val fileName = "REC_${System.currentTimeMillis()}.m4a"
            currentOutputFile = File(recordingsDir, fileName)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(currentOutputFile!!.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            isPaused = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            releaseRecorder()
            return false
        }
    }

    fun pauseRecording() {
        if (isRecording && !isPaused) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.pause()
                    isPaused = true
                }
            } catch (_: Exception) {}
        }
    }

    fun resumeRecording() {
        if (isRecording && isPaused) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.resume()
                    isPaused = false
                }
            } catch (_: Exception) {}
        }
    }

    fun stopRecording(durationSeconds: Long): AudioRecordItem? {
        if (!isRecording) return null
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {}
        releaseRecorder()

        val file = currentOutputFile
        if (file != null && file.exists() && file.length() > 0) {
            return AudioRecordItem(
                id = UUID.randomUUID().toString(),
                fileName = file.name,
                filePath = file.absolutePath,
                durationSeconds = maxOf(1L, durationSeconds),
                timestamp = System.currentTimeMillis()
            )
        }
        return null
    }

    fun getMaxAmplitude(): Int {
        if (!isRecording || isPaused) return 0
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        isRecording = false
        isPaused = false
    }

    // Playback support
    fun playRecording(
        filePath: String,
        onCompletion: () -> Unit,
        onError: () -> Unit
    ): Boolean {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) {
            onError()
            return false
        }

        return try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlayback()
                    onError()
                    true
                }
                start()
            }
            true
        } catch (e: IOException) {
            stopPlayback()
            false
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun deleteRecording(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) file.delete() else true
        } catch (_: Exception) {
            false
        }
    }

    fun release() {
        releaseRecorder()
        stopPlayback()
    }
}
