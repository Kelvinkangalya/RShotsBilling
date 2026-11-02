package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistration(
    val deviceId: String = "",
    val installationId: String = "",
    val deviceName: String = "",
    val platform: String = "",
    val osVersion: String = "",
    val appVersion: String = "",
    val registrationDate: Long = 0,
    val lastLogin: Long = 0,
    val lastSync: Long = 0,
    val status: String = "Pending", // Approved, Pending, Blocked, Revoked
    val businessId: String = "",
    val userId: String = ""
)
