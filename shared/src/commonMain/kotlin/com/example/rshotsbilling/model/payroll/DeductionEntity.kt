package com.example.rshotsbilling.model.payroll

import kotlinx.serialization.Serializable

@Serializable
data class DeductionEntity(
    val name: String = "",
    val amount: Double = 0.0,
    val isFixed: Boolean = true, // true for fixed amount, false for percentage
    val appliesTo: String = "BASIC", // "BASIC" or "GROSS"
    val calculatedAmount: Double = 0.0
)
