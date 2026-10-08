package com.example.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class FridayAiService(
    private val customApiKeyProvider: () -> String = { "" }
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(): String {
        val customKey = customApiKeyProvider()
        if (customKey.isNotBlank()) return customKey.trim()

        return try {
            val buildConfigKey = BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
            buildConfigKey?.trim() ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun isOnlineAiAvailable(): Boolean {
        val key = getEffectiveApiKey()
        return key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY")
    }

    fun getAiStatus(): String {
        return if (isOnlineAiAvailable()) "AI STATUS: READY" else "AI STATUS: OFFLINE"
    }

    suspend fun queryAi(prompt: String, systemContext: String): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            // Local offline tactical AI processing
            return@withContext processOfflineTacticalPrompt(prompt, systemContext)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val systemInstruction = """
                YOU ARE F.R.I.D.A.Y., AN ADVANCED HOLOGRAPHIC SCI-FI TACTICAL AI SYSTEM.
                ALL RESPONSES MUST BE STRICTLY CONCISE, CRISP, AND EXCLUSIVELY UPPERCASE TEXT.
                NO LOWERCASE CHARACTERS. BE DIRECT, PROFESSIONAL, AND TACTICAL.
                CURRENT DEVICE CONTEXT: $systemContext
            """.trimIndent()

            val rootJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstruction\n\nUSER QUERY: $prompt")
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext "ERROR: NEURAL LINK TIMEOUT [CODE ${response.code}]. SWITCHING TO OFFLINE TACTICAL COGNITION.\n" +
                        processOfflineTacticalPrompt(prompt, systemContext)
            }

            val bodyString = response.body?.string().orEmpty()
            val responseJson = JSONObject(bodyString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val rawText = parts.getJSONObject(0).optString("text", "")
                    return@withContext rawText.uppercase(Locale.US).trim()
                }
            }

            return@withContext "TACTICAL LOG: RESPONSE PARSING FAILED. CORE INTACT."
        } catch (e: Exception) {
            return@withContext "COGNITIVE BRIDGE FAILED: ${e.message?.uppercase(Locale.US) ?: "NETWORK UNREACHABLE"}.\n" +
                    processOfflineTacticalPrompt(prompt, systemContext)
        }
    }

    private fun processOfflineTacticalPrompt(prompt: String, systemContext: String): String {
        val query = prompt.trim().uppercase(Locale.US)
        return when {
            query.contains("STATUS") || query.contains("DIAGNOSTIC") ->
                "FRIDAY LOCAL CORE OPERATIONAL. TELEMETRY: $systemContext. ALL PRIMARY SYSTEMS STABLE."
            query.contains("WHO ARE YOU") || query.contains("IDENTITY") ->
                "I AM F.R.I.D.A.Y. — YOUR ADVANCED HOLOGRAPHIC AI TACTICAL INTERFACE. OPERATING IN STANDALONE LOCAL MODE."
            query.contains("SECURITY") || query.contains("DEFENSE") ->
                "SECURITY SUBSYSTEMS NOMINAL. LOCAL CRYPTOGRAPHIC PIN ENCRYPTION ACTIVE. NO UNAUTHORIZED INTRUSIONS DETECTED."
            query.contains("PROTOCOL") ->
                "PROTOCOL ENGINE ACTIVE. YOU CAN DEFINE AND TRIGGER CUSTOM PROTOCOLS VIA THE PROTOCOL PANEL (OPTION 1)."
            query.contains("BATTERY") || query.contains("POWER") ->
                "POWER STATUS CHECKED. CONSULT LEFT SYSTEM ANALYSIS HUD FOR REAL-TIME THERMAL AND DISCHARGE TELEMETRY."
            query.contains("HELP") || query.contains("COMMANDS") ->
                "COMMAND LIST: STATUS, PROTOCOL, DIAGNOSTIC, SECURITY, CLEAR, SCAN, INITIALIZE. YOU MAY ALSO ENTER AN API KEY IN SETTINGS FOR CLOUD COGNITION."
            else ->
                "INPUT PROCESSED: '$query'. LOCAL AI ENGINE LOGGED REQUEST. ALL CORE TELEMETRY AND LOCAL CONTROLS NOMINAL."
        }
    }

    // Future Voice System extension point (Requirement 23)
    fun getSpeechPhraseForEvent(event: String, extra: String = ""): String {
        return when (event.uppercase(Locale.US)) {
            "GREETING" -> "GOOD EVENING. FRIDAY SYSTEM IS READY."
            "INIT_COMPLETE" -> "SYSTEM INITIALIZATION COMPLETE. ALL SUBSYSTEMS VERIFIED."
            "BATTERY_STATUS" -> "BATTERY STATUS: $extra PERCENT."
            "SECURITY_VERIFIED" -> "SECURITY VERIFIED. WELCOME BACK."
            "PROTOCOL_ACCEPTED" -> "PROTOCOL ACCEPTED. EXECUTING SUB-ROUTINE."
            else -> "STANDBY."
        }
    }
}
