package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class VendorAdmin(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val assignedCompanies: List<String> = emptyList(),
    val active: Boolean = true,
    val dateCreated: Long = getCurrentTimestamp()
)

@Serializable
data class ActivationCode(
    val id: String = "",
    val code: String = "",
    val type: String = "COMPANY", // COMPANY, STAFF
    val companyId: String = "",
    val used: Boolean = false,
    val usedBy: String = "",
    val usedDate: Long = 0,
    val expiresAt: Long = 0,
    val createdBy: String = "",
    val createdAt: Long = getCurrentTimestamp()
)

@Serializable
data class Subscription(
    val id: String = "",
    val companyId: String = "",
    val planName: String = "Basic",
    val startDate: Long = 0,
    val expiryDate: Long = 0,
    val status: String = "Active",
    val price: Double = 0.0,
    val maxDevices: Int = 5,
    val maxStaff: Int = 10
)

@Serializable
data class SupportTicket(
    val id: String = "",
    val companyId: String = "",
    val userEmail: String = "",
    val subject: String = "",
    val description: String = "",
    val priority: String = "Medium",
    val status: String = "Open",
    val createdAt: Long = getCurrentTimestamp(),
    val closedAt: Long = 0
)

@Serializable
data class SaaSAuditLog(
    val id: String = "",
    val timestamp: Long = getCurrentTimestamp(),
    val user: String = "",
    val companyId: String = "",
    val device: String = "",
    val action: String = "",
    val metadata: Map<String, String> = emptyMap()
)
