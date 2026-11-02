package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class StaffMember(
    val id: String = "",
    val businessId: String = "", // Link to business
    val email: String = "",
    val displayName: String = "",
    val role: String = "SALES_STAFF", 
    val permissions: List<String> = emptyList(),
    val registeredDevices: List<String> = emptyList(), // Serial numbers allowed for this user
    val activationCode: String = "", // One-time code for staff activation
    val isTemporaryPassword: Boolean = false,
    val setupComplete: Boolean = false,
    val dateAdded: Long = 0
) {
    fun toUserRole(): UserRole = try {
        UserRole.valueOf(role)
    } catch (e: Exception) {
        UserRole.SALES_STAFF
    }
}
