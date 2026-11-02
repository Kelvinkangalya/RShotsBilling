package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val businessId: String = "",
    val name: String = "",
    val ownerEmail: String = "",
    val ownerName: String = "",
    val businessCode: String = "", // Unique code for the business
    val status: String = "Trial", // Active, Trial, Expired, Suspended
    val dateCreated: Long = 0,
    val subscriptionPlan: String = "Basic",
    val maxDevices: Int = 1,
    val modulesEnabled: List<String> = emptyList()
)
