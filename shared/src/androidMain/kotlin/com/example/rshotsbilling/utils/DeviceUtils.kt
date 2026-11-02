package com.example.rshotsbilling.utils

import android.os.Build
import com.example.rshotsbilling.model.InstallationProfile
import android.content.Context
import java.util.UUID

actual fun getDeviceSerialNumber(): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Needs special permission, usually not available. Fallback to generic serial.
            "android-${Build.MODEL}-${Build.ID}"
        } else {
            @Suppress("DEPRECATION")
            Build.SERIAL ?: "unknown-android"
        }
    } catch (e: Exception) {
        "unknown-android"
    }
}

actual fun getInstallationId(): String {
    val prefs = SharedContext.appContext.getSharedPreferences("device_identity", Context.MODE_PRIVATE)
    var id = prefs.getString("installation_id", null)
    if (id == null) {
        id = UUID.randomUUID().toString()
        prefs.edit().putString("installation_id", id).apply()
        println("DEBUG: Installation Created - Android ID: $id")
    } else {
        println("DEBUG: Installation Loaded - Android ID: $id")
    }
    return id
}

actual fun getDeviceInfo(): InstallationProfile {
    return InstallationProfile(
        installationId = getInstallationId(),
        platform = "Android",
        osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        firstInstall = try {
            SharedContext.appContext.packageManager.getPackageInfo(SharedContext.appContext.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            0L
        }
    )
}
