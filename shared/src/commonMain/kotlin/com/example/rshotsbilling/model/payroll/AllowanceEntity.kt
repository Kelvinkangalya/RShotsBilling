package com.example.rshotsbilling.model.payroll

import kotlinx.serialization.Serializable

@Serializable
data class AllowanceEntity(
    val name: String = "",
    val amount: Double = 0.0,
    val isTaxable: Boolean = true,
    val isFixed: Boolean = true, // true for fixed amount, false for percentage (of basic)
    val calculatedAmount: Double = 0.0
)
