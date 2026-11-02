package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.AllowanceEntity
import com.example.rshotsbilling.model.payroll.DeductionEntity
import com.example.rshotsbilling.model.payroll.RuleWithBrackets
import com.example.rshotsbilling.model.payroll.StatutoryBracketEntity
import kotlin.math.max
import kotlin.math.min

/**
 * Dynamic Payroll Calculation Engine for KRA Compliance (2025 Updates)
 */
object PayrollCalculationEngine {

    data class CalculationResult(
        val grossPay: Double,
        val nssfEmployee: Double,
        val nssfEmployer: Double,
        val shifEmployee: Double,
        val shifEmployer: Double,
        val housingLevyEmployee: Double,
        val housingLevyEmployer: Double,
        val taxableIncome: Double,
        val taxCharged: Double,
        val personalRelief: Double,
        val insuranceRelief: Double,
        val payeTax: Double,
        val netPay: Double,
        val nonCashBenefits: Double = 0.0,
        val valueOfQuarters: Double = 0.0,
        val totalAllowances: Double = 0.0,
        val totalDeductions: Double = 0.0,
        val totalEmployerContribution: Double = 0.0,
        val allowanceDetails: List<AllowanceEntity> = emptyList(),
        val deductionDetails: List<DeductionEntity> = emptyList()
    )

    fun calculate(
        basicSalary: Double,
        allowances: List<AllowanceEntity>,
        deductions: List<DeductionEntity>,
        rules: List<RuleWithBrackets>,
        nonCashBenefits: Double = 0.0,
        valueOfQuarters: Double = 0.0,
        mortgageInterest: Double = 0.0,
        prmf: Double = 0.0,
        otherPension: Double = 0.0,
        salaryAdvance: Double = 0.0,
        loanRepayment: Double = 0.0,
        isResident: Boolean = true,
        isHousingLevyExempt: Boolean = false
    ): CalculationResult {
        // 0. Calculate Custom Allowances
        val calculatedAllowances = allowances.map { 
            val amt = if (it.isFixed) it.amount else (basicSalary * it.amount)
            it.copy(calculatedAmount = amt)
        }
        
        var totalTaxableAllowances = 0.0
        var totalNonTaxableAllowances = 0.0
        calculatedAllowances.forEach {
            if (it.isTaxable) totalTaxableAllowances += it.calculatedAmount 
            else totalNonTaxableAllowances += it.calculatedAmount
        }

        val cashGross = basicSalary + totalTaxableAllowances + totalNonTaxableAllowances
        val taxableGrossForStatutory = basicSalary + totalTaxableAllowances + nonCashBenefits + valueOfQuarters

        // 1. Calculate NSSF
        val nssfRule = rules.find { it.rule.name == "NSSF" }
        val nssfEmployee = roundStatutory(calculateRule(taxableGrossForStatutory, nssfRule))
        val nssfEmployer = roundStatutory(calculateRule(taxableGrossForStatutory, nssfRule, isEmployer = true))

        // 2. Calculate SHIF (2.75% of Gross, min 300)
        val shifRule = rules.find { it.rule.name == "SHIF" }
        var shifEmployee = calculateRule(cashGross, shifRule)
        val shifMin = rules.find { it.rule.name == "SHIF_MIN" }?.rule?.employeeRate ?: 300.0
        if (shifEmployee < shifMin && shifRule != null) shifEmployee = shifMin
        shifEmployee = roundStatutory(shifEmployee)
        val shifEmployer = roundStatutory(calculateRule(cashGross, shifRule, isEmployer = true))

        // 3. Calculate Housing Levy (1.5% of Gross)
        val ahlRule = rules.find { it.rule.name == "HOUSING_LEVY" }
        val ahlEmployee = if (isHousingLevyExempt) 0.0 else roundStatutory(calculateRule(cashGross, ahlRule))
        val ahlEmployer = if (isHousingLevyExempt) 0.0 else roundStatutory(calculateRule(cashGross, ahlRule, isEmployer = true))

        // 4. Calculate Taxable Income
        val allowableDeductions = nssfEmployee + shifEmployee + ahlEmployee + mortgageInterest + prmf + otherPension
        val taxablePayFinal = max(0.0, (basicSalary + totalTaxableAllowances + nonCashBenefits + valueOfQuarters) - allowableDeductions)
        val roundedTaxableIncome = kotlin.math.floor(taxablePayFinal)

        // 5. Calculate PAYE
        val payeRule = rules.find { it.rule.name == "PAYE" }
        val taxCharged = calculateBracketedTax(roundedTaxableIncome, payeRule?.brackets ?: emptyList())
        
        val personalReliefRule = rules.find { it.rule.name == "PERSONAL_RELIEF" }
        val personalRelief = if (isResident) (personalReliefRule?.rule?.employeeRate ?: 2400.0) else 0.0
        
        val insuranceRelief = 0.0 
        val payeTax = roundStatutory(max(0.0, taxCharged - personalRelief - insuranceRelief))

        // 6. Calculate Custom Deductions
        val calculatedDeductions = deductions.map {
            val base = if (it.appliesTo == "BASIC") basicSalary else cashGross
            val amt = if (it.isFixed) it.amount else (base * it.amount)
            it.copy(calculatedAmount = amt)
        }
        val totalCustomDeductions = calculatedDeductions.sumOf { it.calculatedAmount }

        // 7. Final Net Pay
        val totalStatutoryDeductions = nssfEmployee + shifEmployee + ahlEmployee + payeTax
        val totalAllDeductions = totalStatutoryDeductions + totalCustomDeductions + salaryAdvance + loanRepayment
        val netPay = cashGross - totalAllDeductions

        return CalculationResult(
            grossPay = cashGross,
            nssfEmployee = nssfEmployee,
            nssfEmployer = nssfEmployer,
            shifEmployee = shifEmployee,
            shifEmployer = shifEmployer,
            housingLevyEmployee = ahlEmployee,
            housingLevyEmployer = ahlEmployer,
            taxableIncome = roundedTaxableIncome,
            taxCharged = taxCharged,
            personalRelief = personalRelief,
            insuranceRelief = insuranceRelief,
            payeTax = payeTax,
            netPay = netPay,
            nonCashBenefits = nonCashBenefits,
            valueOfQuarters = valueOfQuarters,
            totalAllowances = totalTaxableAllowances + totalNonTaxableAllowances,
            totalDeductions = totalAllDeductions,
            totalEmployerContribution = nssfEmployer + shifEmployer + ahlEmployer,
            allowanceDetails = calculatedAllowances,
            deductionDetails = calculatedDeductions
        )
    }

    private fun roundStatutory(amount: Double): Double {
        return kotlin.math.round(amount)
    }

    private fun calculateRule(amount: Double, ruleWithBrackets: RuleWithBrackets?, isEmployer: Boolean = false): Double {
        if (ruleWithBrackets == null) return 0.0
        val rule = ruleWithBrackets.rule
        val rate = if (isEmployer) rule.employerRate else rule.employeeRate

        return when (rule.type) {
            "PERCENTAGE" -> amount * rate
            "FIXED" -> rate
            "BRACKET" -> calculateBracketedTax(amount, ruleWithBrackets.brackets) 
            else -> 0.0
        }
    }

    private fun calculateBracketedTax(amount: Double, brackets: List<StatutoryBracketEntity>): Double {
        var remaining = amount
        var total = 0.0
        val sortedBrackets = brackets.sortedBy { it.lowerLimit }
        for (bracket in sortedBrackets) {
            val bandSize = if (bracket.upperLimit == Double.MAX_VALUE || bracket.upperLimit == 0.0) {
                if (remaining > 0) remaining else 0.0
            } else {
                bracket.upperLimit - bracket.lowerLimit
            }
            val taxable = min(remaining, bandSize)
            total += taxable * bracket.rate
            remaining -= taxable
            if (remaining <= 0) break
        }
        return total
    }
}
