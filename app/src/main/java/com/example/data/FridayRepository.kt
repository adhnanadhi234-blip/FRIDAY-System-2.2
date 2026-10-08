package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AnimationIntensity
import com.example.model.AudioRecordItem
import com.example.model.FridaySettings
import com.example.model.FridayThemeId
import com.example.model.NoteItem
import com.example.model.PriorityLevel
import com.example.model.ProtocolItem
import com.example.model.ReminderItem
import com.example.model.TodoItem
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class FridayRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("friday_system_storage", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "sec_pin_hash"
        private const val KEY_PIN_SALT = "sec_pin_salt"
        private const val KEY_PROTOCOLS = "friday_protocols"
        private const val KEY_TODOS = "friday_todos"
        private const val KEY_NOTES = "friday_notes"
        private const val KEY_REMINDERS = "friday_reminders"
        private const val KEY_RECORDINGS = "friday_recordings"
        private const val KEY_SETTINGS_THEME = "set_theme"
        private const val KEY_SETTINGS_ANIM = "set_anim"
        private const val KEY_SETTINGS_SOUND = "set_sound"
        private const val KEY_SETTINGS_HAPTIC = "set_haptic"
        private const val KEY_SETTINGS_API_KEY = "set_api_key"
    }

    // ----------------- SECURITY PIN -----------------
    fun hasSecurityPin(): Boolean {
        return prefs.contains(KEY_PIN_HASH) && !prefs.getString(KEY_PIN_HASH, "").isNullOrEmpty()
    }

    fun setSecurityPin(pin: String) {
        val salt = UUID.randomUUID().toString()
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    fun verifySecurityPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val inputHash = hashPin(pin, storedSalt)
        return inputHash == storedHash
    }

    private fun hashPin(pin: String, salt: String): String {
        val combined = "$pin:$salt:FRIDAY_SECURE_SALT_2026"
        val bytes = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ----------------- PROTOCOLS -----------------
    fun getProtocols(): List<ProtocolItem> {
        val raw = prefs.getString(KEY_PROTOCOLS, null)
        if (raw.isNullOrBlank()) {
            val defaults = getDefaultProtocols()
            saveProtocols(defaults)
            return defaults
        }
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ProtocolItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ProtocolItem(
                        id = obj.getString("id"),
                        code = obj.getString("code"),
                        title = obj.getString("title"),
                        actionType = obj.getString("actionType"),
                        description = obj.getString("description"),
                        customMessage = obj.optString("customMessage", ""),
                        isSystemPreset = obj.optBoolean("isSystemPreset", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            getDefaultProtocols()
        }
    }

    fun saveProtocols(list: List<ProtocolItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("code", item.code)
                put("title", item.title)
                put("actionType", item.actionType)
                put("description", item.description)
                put("customMessage", item.customMessage)
                put("isSystemPreset", item.isSystemPreset)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PROTOCOLS, array.toString()).apply()
    }

    private fun getDefaultProtocols(): List<ProtocolItem> {
        return listOf(
            ProtocolItem(
                id = "proto_1",
                code = "T450",
                title = "TERMINATE INTERFACE",
                actionType = "CLOSE_APP",
                description = "SHUT DOWN FRIDAY SYSTEM AND RETURN TO ANDROID HOME",
                isSystemPreset = true
            ),
            ProtocolItem(
                id = "proto_2",
                code = "S100",
                title = "DIAGNOSTIC SCAN",
                actionType = "SYSTEM_INFO",
                description = "RUN FULL HARDWARE AND MEMORY AUDIT",
                isSystemPreset = true
            ),
            ProtocolItem(
                id = "proto_3",
                code = "N200",
                title = "TACTICAL MEMO",
                actionType = "LAUNCH_NOTES",
                description = "OPEN ENCRYPTED SYSTEM NOTES REPOSITORY",
                isSystemPreset = true
            ),
            ProtocolItem(
                id = "proto_4",
                code = "R300",
                title = "AUDIO TELEMETRY",
                actionType = "LAUNCH_RECORDER",
                description = "DEPLOY SURVEILLANCE VOICE RECORDER",
                isSystemPreset = true
            ),
            ProtocolItem(
                id = "proto_5",
                code = "C400",
                title = "CHRONO SYNC",
                actionType = "LAUNCH_TIMER",
                description = "ENGAGE DIGITAL COUNTDOWN TIMER",
                isSystemPreset = true
            ),
            ProtocolItem(
                id = "proto_6",
                code = "D500",
                title = "HOLOGRAPHIC DRAW",
                actionType = "LAUNCH_BOARD",
                description = "OPEN FREEBOARD BLACKBOARD MATRIX",
                isSystemPreset = true
            )
        )
    }

    // ----------------- TO-DO ITEMS -----------------
    fun getTodos(): List<TodoItem> {
        val raw = prefs.getString(KEY_TODOS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<TodoItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val priorityName = obj.optString("priority", PriorityLevel.NORMAL.name)
                val priority = try { PriorityLevel.valueOf(priorityName) } catch (_: Exception) { PriorityLevel.NORMAL }
                list.add(
                    TodoItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        priority = priority,
                        isCompleted = obj.optBoolean("isCompleted", false),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        dueDate = obj.optString("dueDate", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTodos(list: List<TodoItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("description", item.description)
                put("priority", item.priority.name)
                put("isCompleted", item.isCompleted)
                put("timestamp", item.timestamp)
                put("dueDate", item.dueDate)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_TODOS, array.toString()).apply()
    }

    // ----------------- NOTES -----------------
    fun getNotes(): List<NoteItem> {
        val raw = prefs.getString(KEY_NOTES, null)
        if (raw.isNullOrBlank()) {
            val defaultNotes = listOf(
                NoteItem(
                    id = "note_sample_1",
                    title = "FRIDAY SYSTEM DIRECTIVE",
                    content = "PRIMARY OBJECTIVE: ASSIST OPERATOR WITH REAL-TIME TACTICAL TELEMETRY, OFFLINE UTILITIES, AND PROTOCOL AUTOMATION.",
                    isPinned = true
                )
            )
            saveNotes(defaultNotes)
            return defaultNotes
        }
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<NoteItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NoteItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        isPinned = obj.optBoolean("isPinned", false),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveNotes(list: List<NoteItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("content", item.content)
                put("isPinned", item.isPinned)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    // ----------------- REMINDERS -----------------
    fun getReminders(): List<ReminderItem> {
        val raw = prefs.getString(KEY_REMINDERS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ReminderItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ReminderItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        timeFormatted = obj.getString("timeFormatted"),
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveReminders(list: List<ReminderItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("timeFormatted", item.timeFormatted)
                put("isEnabled", item.isEnabled)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_REMINDERS, array.toString()).apply()
    }

    // ----------------- VOICE RECORDINGS -----------------
    fun getRecordings(): List<AudioRecordItem> {
        val raw = prefs.getString(KEY_RECORDINGS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<AudioRecordItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AudioRecordItem(
                        id = obj.getString("id"),
                        fileName = obj.getString("fileName"),
                        filePath = obj.getString("filePath"),
                        durationSeconds = obj.getLong("durationSeconds"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveRecordings(list: List<AudioRecordItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("fileName", item.fileName)
                put("filePath", item.filePath)
                put("durationSeconds", item.durationSeconds)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_RECORDINGS, array.toString()).apply()
    }

    // ----------------- SETTINGS -----------------
    fun getSettings(): FridaySettings {
        val themeStr = prefs.getString(KEY_SETTINGS_THEME, FridayThemeId.CYAN.name)
        val animStr = prefs.getString(KEY_SETTINGS_ANIM, AnimationIntensity.MEDIUM.name)
        val sound = prefs.getBoolean(KEY_SETTINGS_SOUND, true)
        val haptic = prefs.getBoolean(KEY_SETTINGS_HAPTIC, true)
        val apiKey = prefs.getString(KEY_SETTINGS_API_KEY, "") ?: ""

        val theme = try { FridayThemeId.valueOf(themeStr ?: FridayThemeId.CYAN.name) } catch (_: Exception) { FridayThemeId.CYAN }
        val anim = try { AnimationIntensity.valueOf(animStr ?: AnimationIntensity.MEDIUM.name) } catch (_: Exception) { AnimationIntensity.MEDIUM }

        return FridaySettings(
            theme = theme,
            animationIntensity = anim,
            soundEffectsEnabled = sound,
            hapticFeedbackEnabled = haptic,
            customGeminiApiKey = apiKey
        )
    }

    fun saveSettings(settings: FridaySettings) {
        prefs.edit()
            .putString(KEY_SETTINGS_THEME, settings.theme.name)
            .putString(KEY_SETTINGS_ANIM, settings.animationIntensity.name)
            .putBoolean(KEY_SETTINGS_SOUND, settings.soundEffectsEnabled)
            .putBoolean(KEY_SETTINGS_HAPTIC, settings.hapticFeedbackEnabled)
            .putString(KEY_SETTINGS_API_KEY, settings.customGeminiApiKey)
            .apply()
    }

    // ----------------- SYSTEM RESET -----------------
    fun resetData(
        resetNotes: Boolean,
        resetTodos: Boolean,
        resetProtocols: Boolean,
        resetSettings: Boolean,
        resetSecurityPin: Boolean = false
    ) {
        val editor = prefs.edit()
        if (resetNotes) editor.remove(KEY_NOTES)
        if (resetTodos) editor.remove(KEY_TODOS)
        if (resetProtocols) editor.remove(KEY_PROTOCOLS)
        if (resetSettings) {
            editor.remove(KEY_SETTINGS_THEME)
            editor.remove(KEY_SETTINGS_ANIM)
            editor.remove(KEY_SETTINGS_SOUND)
            editor.remove(KEY_SETTINGS_HAPTIC)
            editor.remove(KEY_SETTINGS_API_KEY)
        }
        if (resetSecurityPin) {
            editor.remove(KEY_PIN_HASH)
            editor.remove(KEY_PIN_SALT)
        }
        editor.apply()
    }
}
