package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class InstallationProfile(
    val installationId: String = "",
    val platform: String = "", // Android / Desktop
    val osVersion: String = "",
    val appVersion: String = "1.0.0",
    val deviceName: String = "",
    val manufacturer: String = "", // Android only
    val model: String = "",        // Android only
    val firstInstall: Long = 0,
    var lastLogin: Long = 0,
    var lastSync: Long = 0,
    var firebaseUid: String = "",
    var businessId: String = "",
    var currentUser: String = "",
    val active: Boolean = true,
    val approved: Boolean = true
)
