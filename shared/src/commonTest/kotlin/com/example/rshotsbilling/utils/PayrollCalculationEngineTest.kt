package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PayrollCalculationEngineTest {

    @Test
    fun testStandardPayroll2025() {
        // Setup 2025 Rules
        val rules = listOf(
            RuleWithBrackets(
                rule = StatutoryDeductionRuleEntity(id = "nssf", name = "NSSF", type = "BRACKET"),
                brackets = listOf(
                    StatutoryBracketEntity(ruleId = "nssf", lowerLimit = 0.0, upperLimit = 8000.0, rate = 0.06),
                    StatutoryBracketEntity(ruleId = "nssf", lowerLimit = 8000.0, upperLimit = 72000.0, rate = 0.06)
                )
            ),
            RuleWithBrackets(
                rule = StatutoryDeductionRuleEntity(id = "shif", name = "SHIF", type = "PERCENTAGE", employeeRate = 0.0275),
                brackets = emptyList()
            ),
            RuleWithBrackets(
                rule = StatutoryDeductionRuleEntity(id = "ahl", name = "HOUSING_LEVY", type = "PERCENTAGE", employeeRate = 0.015, employerRate = 0.015),
                brackets = emptyList()
            ),
            RuleWithBrackets(
                rule = StatutoryDeductionRuleEntity(id = "paye", name = "PAYE", type = "BRACKET"),
                brackets = listOf(
                    StatutoryBracketEntity(ruleId = "paye", lowerLimit = 0.0, upperLimit = 24000.0, rate = 0.10),
                    StatutoryBracketEntity(ruleId = "paye", lowerLimit = 24000.0, upperLimit = 32333.0, rate = 0.25),
                    StatutoryBracketEntity(ruleId = "paye", lowerLimit = 32333.0, upperLimit = 500000.0, rate = 0.30),
                    StatutoryBracketEntity(ruleId = "paye", lowerLimit = 500000.0, upperLimit = 800000.0, rate = 0.325),
                    StatutoryBracketEntity(ruleId = "paye", lowerLimit = 800000.0, upperLimit = Double.MAX_VALUE, rate = 0.35)
                )
            ),
            RuleWithBrackets(
                rule = StatutoryDeductionRuleEntity(id = "pr", name = "PERSONAL_RELIEF", type = "FIXED", employeeRate = 2400.0),
                brackets = emptyList()
            )
        )

        // Test with 100,000 KES Gross
        val result = PayrollCalculationEngine.calculate(
            basicSalary = 100000.0,
            allowances = emptyList(),
            deductions = emptyList(),
            rules = rules,
            isResident = true
        )

        // Expected:
        // NSSF: 6% of 72,000 = 4,320
        // SHIF: 2.75% of 100,000 = 2,750
        // AHL: 1.5% of 100,000 = 1,500
        // Taxable Pay: 100,000 - (4320 + 2750 + 1500) = 91,430
        // PAYE before relief:
        // 24,000 * 0.10 = 2,400
        // 8,333 * 0.25 = 2,083.25
        // 59,097 * 0.30 = 17,729.1
        // Total PAYE = 22,212.35
        // PAYE after relief (2,400) = 19,812.35
        
        assertEquals(4320.0, result.nssfEmployee, "NSSF should be 4320")
        assertEquals(2750.0, result.shifEmployee, "SHIF should be 2750")
        assertEquals(1500.0, result.housingLevyEmployee, "AHL should be 1500")
        assertEquals(91430.0, result.taxableIncome, "Taxable Pay should be 91430")
        assertTrue(result.payeTax >= 19812.0 && result.payeTax <= 19813.0, "PAYE should be approx 19812")
    }
}
