package com.example.rshotsbilling.model.payroll

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeEntity(
    val id: String = "",
    val fullName: String = "",
    val nationalId: String = "",
    val kraPin: String = "",
    val pnNumber: String = "", 
    val nssfNumber: String = "",
    val shifNumber: String = "", 
    val phone: String = "",
    val email: String = "",
    val department: String = "",
    val position: String = "",
    val termsOfService: String = "P&P", 
    val basicSalary: Double = 0.0,
    val salaryType: String = "MONTHLY", 
    val bankName: String = "",
    val bankBranch: String = "",
    val accountNumber: String = "",
    val employmentDate: Long = 0,
    val status: String = "ACTIVE", 
    val isResident: Boolean = true,
    val isDisabled: Boolean = false,
    val employeeType: String = "Primary Employee", // Primary, Secondary, Director etc
    val exemptionCertificateNumber: String = "",
    val typeOfHousing: String = "None", // Employer Provided, None, Rented, Owned
    val housingLevyExempt: Boolean = false,
    val employeeNumber: String = "",
    val jobTitle: String = "",
    val employmentType: String = "Permanent", // Permanent, Contract, Casual
    val taxStatus: String = "Resident", // Resident, Non-Resident
    val pwdStatus: Boolean = false,
    val paymentMethod: String = "Bank", // Bank, M-Pesa, Cash
    val saccoDeduction: Double = 0.0,
    val loanRepayment: Double = 0.0,
    val loanBalance: Double = 0.0,
    val salaryAdvanceBalance: Double = 0.0,
    val nonCashBenefits: Double = 0.0,
    val valueOfQuarters: Double = 0.0,
    val mortgageInterest: Double = 0.0,
    val prmfContribution: Double = 0.0,
    val otherPension: Double = 0.0,
    val allowances: List<AllowanceEntity> = emptyList(),
    val deductions: List<DeductionEntity> = emptyList(),
    val createdAt: Long = getCurrentTimestamp()
)
