package com.example.rshotsbilling.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PayrollCalculatorTest {

    @Test
    fun testPayrollCalculation_100k() {
        // For an employee earning KES 100,000 in 2025
        val gross = 100000.0
        val result = PayrollCalculator.calculate(gross)

        // NSSF 2025: 6% of 8k (480) + 6% of (72k-8k=64k) (3840) = 4320
        assertEquals(4320.0, result.nssf, 0.01)

        // SHIF: 2.75% of 100k = 2750
        assertEquals(2750.0, result.shif, 0.01)

        // Housing Levy: 1.5% of 100k = 1500
        assertEquals(1500.0, result.housingLevy, 0.01)

        // Taxable Pay: 100,000 - 4320 - 2750 - 1500 = 91430
        assertEquals(91430.0, result.taxablePay, 0.01)

        // PAYE Calculation on 91,430:
        // First 24,000 @ 10% = 2,400
        // Next 8,333 @ 25% = 2,083.25
        // Remaining (91,430 - 24,000 - 8,333 = 59,097) @ 30% = 17,729.10
        // Total PAYE Before Relief = 2,400 + 2,083.25 + 17,729.10 = 22,212.35
        assertEquals(22212.35, result.payeBeforeRelief, 0.01)

        // After Relief (22,212.35 - 2,400) = 19,812.35
        assertEquals(19812.35, result.payeAfterRelief, 0.01)

        // Net Pay: 100,000 - 4,320 - 2,750 - 1,500 - 19,812.35 = 71,617.65
        assertEquals(71617.65, result.netPay, 0.01)
    }

    @Test
    fun testLowSalary_SHIF_Minimum() {
        val gross = 5000.0 // Very low salary
        val result = PayrollCalculator.calculate(gross)
        
        // SHIF 2.75% of 5000 is 137.5, but minimum is 300
        assertEquals(300.0, result.shif, 0.01)
    }
}
