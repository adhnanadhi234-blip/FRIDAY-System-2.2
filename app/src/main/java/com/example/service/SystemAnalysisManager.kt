package com.example.service

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import android.view.WindowManager
import com.example.model.HardwareTelemetry
import java.util.Locale

class SystemAnalysisManager(private val context: Context) {

    fun readCurrentTelemetry(): HardwareTelemetry {
        // Battery info
        var batteryPercent = 100
        var batteryTempC = 25.0f
        var isCharging = false

        try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            batteryStatus?.let { intent ->
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryPercent = ((level.toFloat() / scale.toFloat()) * 100).toInt()
                }

                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                if (temp > 0) {
                    batteryTempC = temp / 10.0f
                }

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        } catch (_: Exception) {}

        // Memory info
        var ramTotalMb = 4096L
        var ramAvailableMb = 2048L
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            ramTotalMb = memInfo.totalMem / (1024 * 1024)
            ramAvailableMb = memInfo.availMem / (1024 * 1024)
        } catch (_: Exception) {}

        // Storage info
        var storageTotalGb = 64f
        var storageAvailableGb = 32f
        var storageUsedGb = 32f
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - availBytes

            val gbFactor = 1024.0f * 1024.0f * 1024.0f
            storageTotalGb = (totalBytes / gbFactor)
            storageAvailableGb = (availBytes / gbFactor)
            storageUsedGb = (usedBytes / gbFactor)
        } catch (_: Exception) {}

        // CPU & architecture
        val cpuArch = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "ARM64-V8A"
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val is64Bit = Process.is64Bit()

        // Display
        var screenRes = "1920X1080"
        var refreshRate = 60.0f
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager?.currentWindowMetrics?.bounds
                if (bounds != null) {
                    val w = bounds.width()
                    val h = bounds.height()
                    screenRes = "${maxOf(w, h)}X${minOf(w, h)}"
                }
            } else {
                val display = windowManager?.defaultDisplay
                val metrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                display?.getRealMetrics(metrics)
                screenRes = "${maxOf(metrics.widthPixels, metrics.heightPixels)}X${minOf(metrics.widthPixels, metrics.heightPixels)}"
            }

            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay
            }
            display?.mode?.refreshRate?.let { rate ->
                if (rate > 0) refreshRate = rate
            }
        } catch (_: Exception) {}

        // Network & Wi-Fi
        var networkType = "OFFLINE"
        var isWifi = false
        try {
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connManager?.activeNetwork
            val caps = connManager?.getNetworkCapabilities(activeNetwork)
            if (caps != null) {
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    networkType = "WI-FI ACTIVE"
                    isWifi = true
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    networkType = "CELLULAR ACTIVE"
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    networkType = "ETHERNET ACTIVE"
                } else {
                    networkType = "ONLINE"
                }
            }
        } catch (_: Exception) {}

        // Bluetooth
        var isBtEnabled = false
        try {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (btAdapter != null && btAdapter.isEnabled) {
                isBtEnabled = true
            }
        } catch (_: Exception) {}

        // Uptime
        val uptimeMs = SystemClock.elapsedRealtime()
        val hours = uptimeMs / (1000 * 60 * 60)
        val minutes = (uptimeMs % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (uptimeMs % (1000 * 60)) / 1000
        val uptimeFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        // App memory usage
        val appMemMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)

        // System condition
        // Optimal if battery > 15% and RAM available > 200MB and Temp < 45C
        val isWarning = batteryPercent < 15 || ramAvailableMb < 200 || batteryTempC > 46.0f
        val condition = if (isWarning) "WARNING" else "OPTIMAL"

        return HardwareTelemetry(
            batteryPercent = batteryPercent,
            batteryTemperatureC = batteryTempC,
            isCharging = isCharging,
            ramTotalMb = ramTotalMb,
            ramAvailableMb = ramAvailableMb,
            storageTotalGb = storageTotalGb,
            storageAvailableGb = storageAvailableGb,
            storageUsedGb = storageUsedGb,
            manufacturer = Build.MANUFACTURER.uppercase(Locale.US),
            model = Build.MODEL.uppercase(Locale.US),
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            cpuArch = cpuArch.uppercase(Locale.US),
            cpuCores = cpuCores,
            is64Bit = is64Bit,
            screenResolution = screenRes,
            refreshRateHz = refreshRate,
            networkType = networkType,
            isWifiConnected = isWifi,
            isBluetoothEnabled = isBtEnabled,
            uptimeFormatted = uptimeFormatted,
            appMemoryMb = appMemMb,
            systemCondition = condition
        )
    }
}
