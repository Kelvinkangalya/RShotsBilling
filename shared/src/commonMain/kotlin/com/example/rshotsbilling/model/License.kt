package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class License(
    val licenseId: String = "",
    val businessId: String = "",
    val businessCode: String = "",
    val activationCode: String = "",
    val subscription: String = "Trial",
    val expiryDate: Long = 0,
    val maxDevices: Int = 1,
    val enabledModules: List<String> = emptyList(),
    val status: String = "Active" // Active, Trial, Expired, Suspended
)
