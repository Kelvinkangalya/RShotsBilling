package com.example.rshotsbilling.utils

import kotlinx.browser.localStorage
import org.w3c.dom.set
import org.w3c.dom.get

actual fun getDeviceSerialNumber(): String {
    val existingId = localStorage["device_serial_id"]
    if (existingId != null) return existingId
    
    val newId = "WEB_" + (0..1000000).random().toString() + "_" + kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    localStorage["device_serial_id"] = newId
    return newId
}

actual fun getInstallationId(): String {
    val existingId = localStorage["installation_id"]
    if (existingId != null) return existingId
    
    val newId = "INST_" + (0..1000000).random().toString() + "_" + kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    localStorage["installation_id"] = newId
    return newId
}

actual fun getDeviceInfo(): com.example.rshotsbilling.model.InstallationProfile {
    return com.example.rshotsbilling.model.InstallationProfile(
        installationId = getInstallationId(),
        platform = "Web",
        osVersion = "Unknown Browser",
        deviceName = "Web Browser",
        manufacturer = "Unknown",
        model = "Web"
    )
}
