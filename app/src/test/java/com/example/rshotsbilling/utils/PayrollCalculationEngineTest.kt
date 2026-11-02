package com.example.rshotsbilling.utils

import com.example.rshotsbilling.data.local.RuleWithBrackets
import com.example.rshotsbilling.model.payroll.StatutoryBracketEntity
import com.example.rshotsbilling.model.payroll.StatutoryDeductionRuleEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PayrollCalculationEngineTest {

    private fun getMockRules(): List<RuleWithBrackets> {
        val currentTime = System.currentTimeMillis()
        
        val nssfId = "nssf-id"
        val nssfRule = RuleWithBrackets(
            StatutoryDeductionRuleEntity(id = nssfId, name = "NSSF", type = "BRACKET", employeeRate = 0.06, employerRate = 0.06, effectiveFrom = currentTime),
            listOf(
                StatutoryBracketEntity(ruleId = nssfId, lowerLimit = 0.0, upperLimit = 9000.0, rate = 0.06),
                StatutoryBracketEntity(ruleId = nssfId, lowerLimit = 9000.0, upperLimit = 108000.0, rate = 0.06)
            )
        )

        val shifRule = RuleWithBrackets(
            StatutoryDeductionRuleEntity(name = "SHIF", type = "PERCENTAGE", employeeRate = 0.0275, effectiveFrom = currentTime),
            emptyList()
        )

        val ahlRule = RuleWithBrackets(
            StatutoryDeductionRuleEntity(name = "HOUSING_LEVY", type = "PERCENTAGE", employeeRate = 0.015, employerRate = 0.015, effectiveFrom = currentTime),
            emptyList()
        )

        val payeId = "paye-id"
        val payeRule = RuleWithBrackets(
            StatutoryDeductionRuleEntity(id = payeId, name = "PAYE", type = "BRACKET", effectiveFrom = currentTime),
            listOf(
                StatutoryBracketEntity(ruleId = payeId, lowerLimit = 0.0, upperLimit = 24000.0, rate = 0.10),
                StatutoryBracketEntity(ruleId = payeId, lowerLimit = 24000.0, upperLimit = 32333.0, rate = 0.25),
                StatutoryBracketEntity(ruleId = payeId, lowerLimit = 32333.0, upperLimit = 500000.0, rate = 0.30),
                StatutoryBracketEntity(ruleId = payeId, lowerLimit = 500000.0, upperLimit = 800000.0, rate = 0.325),
                StatutoryBracketEntity(ruleId = payeId, lowerLimit = 800000.0, upperLimit = Double.MAX_VALUE, rate = 0.35)
            )
        )

        return listOf(nssfRule, shifRule, ahlRule, payeRule)
    }

    @Test
    fun testCalculation_50k() {
        val result = PayrollCalculationEngine.calculate(
            basicSalary = 50000.0,
            allowances = 0.0,
            overtime = 0.0,
            bonus = 0.0,
            rules = getMockRules()
        )

        // NSSF: 50,000 * 0.06 = 3,000
        assertEquals(3000.0, result.nssfEmployee, 0.01)
        assertEquals(3000.0, result.nssfEmployer, 0.01)

        // SHIF: 2.75% of 50k = 1375
        assertEquals(1375.0, result.shifEmployee, 0.01)

        // AHL: 1.5% of 50k = 750
        assertEquals(750.0, result.housingLevyEmployee, 0.01)
        assertEquals(750.0, result.housingLevyEmployer, 0.01)

        // Taxable Income: 50,000 - 3,000 - 1,375 - 750 = 44,875
        assertEquals(44875.0, result.taxableIncome, 0.01)

        // Tax Charged on 44,875:
        // 24,000 @ 10% = 2,400
        // 8,333 @ 25% = 2,083.25
        // (44,875 - 24,000 - 8,333 = 12,542) @ 30% = 3,762.6
        // Total = 2,400 + 2,083.25 + 3,762.6 = 8,245.85
        assertEquals(8245.85, result.taxCharged, 0.01)

        // Reliefs: 2400 + (1375 * 0.15 = 206.25) = 2,606.25
        assertEquals(2400.0, result.personalRelief, 0.01)
        assertEquals(206.25, result.insuranceRelief, 0.01)

        // PAYE Tax: 8,245.85 - 2,606.25 = 5,639.6
        assertEquals(5639.6, result.payeTax, 0.01)

        // Net Pay: 50,000 - 3,000 - 1,375 - 750 - 5,639.6 = 39,235.4
        assertEquals(39235.4, result.netPay, 0.01)
    }

    @Test
    fun testMaxNSSF() {
        val result = PayrollCalculationEngine.calculate(
            basicSalary = 150000.0,
            allowances = 0.0,
            overtime = 0.0,
            bonus = 0.0,
            rules = getMockRules()
        )

        // Max NSSF: 6,480
        assertEquals(6480.0, result.nssfEmployee, 0.01)
        assertEquals(6480.0, result.nssfEmployer, 0.01)
    }
}
