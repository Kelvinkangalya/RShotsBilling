package com.example.rshotsbilling.model.payroll

import kotlinx.serialization.Serializable

@Serializable
data class StatutoryDeductionRuleEntity(
    val id: String = "",
    val name: String = "", 
    val type: String = "PERCENTAGE", 
    val employeeRate: Double = 0.0,
    val employerRate: Double = 0.0,
    val appliesTo: String = "GROSS", 
    val effectiveFrom: Long = 0,
    val effectiveTo: Long? = null,
    val isActive: Boolean = true,
    val category: String = "STATUTORY", // STATUTORY, ALLOWANCE, DEDUCTION
    val isDefault: Boolean = false
)

@Serializable
data class RuleWithBrackets(
    val rule: StatutoryDeductionRuleEntity,
    val brackets: List<StatutoryBracketEntity>
)
