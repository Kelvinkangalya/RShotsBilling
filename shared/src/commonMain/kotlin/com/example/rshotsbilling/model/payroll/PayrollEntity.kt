package com.example.rshotsbilling.model.payroll

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class PayrollEntity(
    val id: String = "",
    val employeeId: String = "",
    val month: Int = 0, 
    val year: Int = 0,
    val grossSalary: Double = 0.0,
    val basicSalary: Double = 0.0,
    val overtimeAmount: Double = 0.0,
    val bonusAmount: Double = 0.0,
    val allowances: Double = 0.0,
    val nssfDeduction: Double = 0.0,
    val shifDeduction: Double = 0.0,
    val housingLevyDeduction: Double = 0.0,
    val employerNssf: Double = 0.0,
    val employerShif: Double = 0.0,
    val employerHousingLevy: Double = 0.0,
    val taxablePay: Double = 0.0,
    val taxCharged: Double = 0.0,
    val payeAmount: Double = 0.0,
    val personalRelief: Double = 2400.0,
    val insuranceRelief: Double = 0.0,
    val mortgageInterest: Double = 0.0,
    val prmfContribution: Double = 0.0,
    val otherPension: Double = 0.0,
    val salaryAdvance: Double = 0.0,
    val loanRepayment: Double = 0.0,
    val carBenefit: Double = 0.0,
    val valueOfMeals: Double = 0.0,
    val nonCashBenefits: Double = 0.0,
    val housingBenefit: Double = 0.0,
    val valueOfQuarters: Double = 0.0,
    val otherBenefits: Double = 0.0,
    val otherDeductions: Double = 0.0,
    val allowanceDetails: List<AllowanceEntity> = emptyList(),
    val deductionDetails: List<DeductionEntity> = emptyList(),
    val totalEmployerContribution: Double = 0.0,
    val netPay: Double = 0.0,
    val status: String = "PENDING", // PENDING, PROCESSED, FINALIZED, P9_ADDED
    val processedBy: String = "",
    val processedAt: Long = getCurrentTimestamp(),
    val paidAt: Long? = null
)
