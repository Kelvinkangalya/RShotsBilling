package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val id: String = "",
    val email: String = "",
    val requestedAt: Long = 0,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val processedBy: String? = null,
    val processedAt: Long? = null,
    val businessId: String = ""
)
