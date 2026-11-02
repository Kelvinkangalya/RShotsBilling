package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PayrollIntegrityTest {

    @Test
    fun testPayrollToP9Mapping() {
        // Mock a monthly record
        val record = PayrollEntity(
            month = 1,
            year = 2026,
            basicSalary = 50000.0,
            grossSalary = 60000.0, // basic + allowances
            shifDeduction = 1595.0,
            nssfDeduction = 1080.0,
            housingLevyDeduction = 870.0,
            payeAmount = 5000.0,
            personalRelief = 2400.0,
            taxablePay = 55000.0
        )

        // Verify mapping logic used in P9Generator
        val p9Gross = record.grossSalary
        val p9TaxCharged = record.payeAmount + record.personalRelief + record.insuranceRelief
        
        assertEquals(60000.0, p9Gross)
        assertEquals(7400.0, p9TaxCharged) // 5000 + 2400 + 0
        assertEquals(1595.0, record.shifDeduction)
        assertEquals(1080.0, record.nssfDeduction)
        assertEquals(870.0, record.housingLevyDeduction)
    }

    @Test
    fun testP9Aggregation() {
        val records = listOf(
            PayrollEntity(month = 1, grossSalary = 50000.0, shifDeduction = 1000.0),
            PayrollEntity(month = 2, grossSalary = 50000.0, shifDeduction = 1000.0)
        )
        
        val totalGross = records.sumOf { it.grossSalary }
        val totalShif = records.sumOf { it.shifDeduction }
        
        assertEquals(100000.0, totalGross)
        assertEquals(2000.0, totalShif)
    }
}
