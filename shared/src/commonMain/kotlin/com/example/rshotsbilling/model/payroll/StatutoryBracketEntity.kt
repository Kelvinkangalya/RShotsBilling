package com.example.rshotsbilling.model.payroll

import kotlinx.serialization.Serializable

@Serializable
data class StatutoryBracketEntity(
    val id: String = "",
    val ruleId: String = "",
    val lowerLimit: Double = 0.0,
    val upperLimit: Double = 0.0, 
    val rate: Double = 0.0
)
