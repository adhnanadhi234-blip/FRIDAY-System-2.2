package com.example.model

enum class FridayThemeId {
    CYAN,
    CRIMSON_GOLD,
    EMERALD,
    AMBER,
    VIOLET
}

enum class AnimationIntensity {
    LOW,
    MEDIUM,
    HIGH
}

enum class PriorityLevel {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

data class ProtocolItem(
    val id: String,
    val code: String,
    val title: String,
    val actionType: String, // E.g., "SYSTEM_INFO", "LAUNCH_RECORDER", "LAUNCH_NOTES", "LAUNCH_TODO", "LAUNCH_TIMER", "CUSTOM_ALERT", "CLOSE_APP"
    val description: String,
    val customMessage: String = "",
    val isSystemPreset: Boolean = false
)

data class TodoItem(
    val id: String,
    val title: String,
    val description: String = "",
    val priority: PriorityLevel = PriorityLevel.NORMAL,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val dueDate: String = ""
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReminderItem(
    val id: String,
    val title: String,
    val timeFormatted: String,
    val isEnabled: Boolean = true
)

data class AudioRecordItem(
    val id: String,
    val fileName: String,
    val filePath: String,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class FridaySettings(
    val theme: FridayThemeId = FridayThemeId.CYAN,
    val animationIntensity: AnimationIntensity = AnimationIntensity.MEDIUM,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val autoLockMinutes: Int = 0, // 0 means disabled
    val customGeminiApiKey: String = ""
)

data class HardwareTelemetry(
    val batteryPercent: Int = 100,
    val batteryTemperatureC: Float = 28.0f,
    val isCharging: Boolean = false,
    val ramTotalMb: Long = 0,
    val ramAvailableMb: Long = 0,
    val storageTotalGb: Float = 0f,
    val storageAvailableGb: Float = 0f,
    val storageUsedGb: Float = 0f,
    val manufacturer: String = "CORE",
    val model: String = "TERMINAL",
    val androidVersion: String = "11+",
    val apiLevel: Int = 30,
    val cpuArch: String = "ARM64",
    val cpuCores: Int = 8,
    val is64Bit: Boolean = true,
    val screenResolution: String = "1920X1080",
    val refreshRateHz: Float = 60f,
    val networkType: String = "OFFLINE",
    val isWifiConnected: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val uptimeFormatted: String = "00:00:00",
    val appMemoryMb: Long = 0,
    val systemCondition: String = "OPTIMAL" // "OPTIMAL" or "WARNING"
)

enum class MainOption {
    PROTOCOL,
    TODO,
    BLACK_BOARD,
    NOTES,
    SYSTEM_RESET,
    SETTINGS,
    CLOCK_REMINDER,
    VOICE_RECORDER
}
