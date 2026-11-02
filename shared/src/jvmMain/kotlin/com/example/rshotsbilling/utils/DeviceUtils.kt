package com.example.rshotsbilling.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import com.example.rshotsbilling.model.InstallationProfile
import java.util.prefs.Preferences
import java.util.UUID

actual fun getDeviceSerialNumber(): String {
    // BYPASS slow WMIC calls for now to eliminate lag
    return getStableDesktopFallback()
}

private fun getStableDesktopFallback(): String {
    return try {
        val userName = System.getProperty("user.name") ?: "user"
        // Avoid slow DNS lookups with getLocalHost().hostName
        val hostName = System.getenv("COMPUTERNAME") ?: "PC" 
        
        // Create a simple stable ID hash
        val raw = "$userName@$hostName"
        val hash = raw.hashCode()
        "PC-" + java.lang.Integer.toHexString(if (hash < 0) -hash else hash).uppercase()
    } catch (e: Exception) {
        "FALLBACK-PC-ID"
    }
}

actual fun getInstallationId(): String {
    val prefs = Preferences.userRoot().node("com.example.rshotsbilling.identity")
    var id = prefs.get("installation_id", null)
    if (id == null) {
        id = UUID.randomUUID().toString()
        prefs.put("installation_id", id)
        println("DEBUG: Installation Created - JVM ID: $id")
    } else {
        println("DEBUG: Installation Loaded - JVM ID: $id")
    }
    return id
}

actual fun getDeviceInfo(): InstallationProfile {
    val osName = System.getProperty("os.name") ?: "Unknown OS"
    val osVersion = System.getProperty("os.version") ?: ""
    val computerName = System.getenv("COMPUTERNAME") ?: "Desktop-PC"
    
    return InstallationProfile(
        installationId = getInstallationId(),
        platform = "Desktop",
        osVersion = "$osName $osVersion",
        deviceName = computerName,
        model = computerName,
        firstInstall = 0L
    )
}
