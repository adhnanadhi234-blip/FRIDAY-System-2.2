package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticSoundManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 45)
        } catch (_: Exception) {
            // ToneGenerator might not be permitted on some minimal devices
        }
    }

    fun playClick(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(15)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_BEEP, 30)
        }
    }

    fun playConfirm(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(35)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_ACK, 70)
        }
    }

    fun playError(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(80)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_NACK, 120)
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (_: Exception) {
            // Ignore tone failure
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
