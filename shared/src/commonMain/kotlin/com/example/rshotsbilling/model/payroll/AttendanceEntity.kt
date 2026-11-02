package com.example.rshotsbilling.model.payroll

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceEntity(
    val id: String = "",
    val employeeId: String = "",
    val date: Long = 0, // Midnight of the day
    val clockInTime: Long? = null,
    val clockOutTime: Long? = null,
    val status: String = "PRESENT", // PRESENT, ABSENT, LATE, ON_LEAVE
    val hoursWorked: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val notes: String? = null
)
