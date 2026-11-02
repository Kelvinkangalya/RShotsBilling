package com.example.rshotsbilling.model.payroll

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class LeaveEntity(
    val id: String = "",
    val employeeId: String = "",
    val leaveType: String = "ANNUAL", // ANNUAL, SICK, MATERNITY, PATERNITY, UNPAID
    val startDate: Long = 0,
    val endDate: Long = 0,
    val totalDays: Int = 0,
    val reason: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val approvedBy: String? = null,
    val createdAt: Long = getCurrentTimestamp()
)
