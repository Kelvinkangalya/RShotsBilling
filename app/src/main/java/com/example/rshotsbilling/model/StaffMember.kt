package com.example.rshotsbilling.model

data class AndroidStaffMember(
    val id: String,
    val displayName: String,
    val email: String,
    val role: String,
    val setupComplete: Boolean = true,
    val isTemporaryPassword: Boolean = false
)
