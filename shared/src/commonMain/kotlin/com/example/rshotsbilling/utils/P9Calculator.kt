package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.PayrollEntity
import kotlinx.serialization.Serializable

@Serializable
data class P9MonthData(
    val month: Int,
    val monthName: String,
    val basicSalary: Double = 0.0,
    val benefitsNonCash: Double = 0.0,
    val valueOfQuarters: Double = 0.0,
    val totalGrossPay: Double = 0.0,
    val ahl: Double = 0.0,
    val shif: Double = 0.0,
    val nssf: Double = 0.0,
    val interest: Double = 0.0,
    val chargeablePay: Double = 0.0,
    val taxCharged: Double = 0.0,
    val personalRelief: Double = 0.0,
    val insuranceRelief: Double = 0.0,
    val payeTax: Double = 0.0,
    val exists: Boolean = false
)

@Serializable
data class P9AnnualSummary(
    val employeeId: String,
    val year: Int,
    val months: List<P9MonthData>,
    val totalA: Double,
    val totalB: Double,
    val totalC: Double,
    val totalD: Double,
    val totalE1: Double,
    val totalE2: Double,
    val totalE3: Double,
    val totalG: Double,
    val totalK: Double,
    val totalL: Double,
    val totalM: Double,
    val totalN: Double,
    val totalO: Double
)

object P9Calculator {
    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun calculateAnnualP9(employeeId: String, year: Int, records: List<PayrollEntity>): P9AnnualSummary {
        KMPLogger.log("P9 TOTAL: employeeId=$employeeId, year=$year, monthsFound=${records.size}")
        
        val p9Months = mutableListOf<P9MonthData>()
        
        for (i in 1..12) {
            val record = records.find { it.month == i }
            if (record != null) {
                // EXPLICIT MAPPING FROM SOURCE PAYROLL RECORD
                val grossD = record.grossSalary + record.nonCashBenefits + record.valueOfQuarters
                val taxL = if (record.taxCharged > 0.0) record.taxCharged else (record.payeAmount + record.personalRelief + record.insuranceRelief)
                
                KMPLogger.log("P9 MONTHLY RECORD: month=$i, gross=$grossD, sha=${record.shifDeduction}, nssf=${record.nssfDeduction}, levy=${record.housingLevyDeduction}, paye=${record.payeAmount}")

                p9Months.add(P9MonthData(
                    month = i,
                    monthName = monthNames[i-1],
                    basicSalary = record.basicSalary,
                    benefitsNonCash = record.nonCashBenefits,
                    valueOfQuarters = record.valueOfQuarters,
                    totalGrossPay = grossD,
                    ahl = record.housingLevyDeduction,
                    shif = record.shifDeduction,
                    nssf = record.nssfDeduction,
                    interest = record.mortgageInterest,
                    chargeablePay = 0.0, // (K) - ACTUAL CONTRIBUTION - SHOULD BE BLANK AS PER REQUIREMENT
                    taxCharged = taxL,
                    personalRelief = record.personalRelief,
                    insuranceRelief = record.insuranceRelief,
                    payeTax = record.payeAmount,
                    exists = true
                ))
            } else {
                p9Months.add(P9MonthData(month = i, monthName = monthNames[i-1], exists = false))
            }
        }

        val summary = P9AnnualSummary(
            employeeId = employeeId,
            year = year,
            months = p9Months,
            totalA = p9Months.sumOf { it.basicSalary },
            totalB = p9Months.sumOf { it.benefitsNonCash },
            totalC = p9Months.sumOf { it.valueOfQuarters },
            totalD = p9Months.sumOf { it.totalGrossPay },
            totalE1 = p9Months.sumOf { it.ahl },
            totalE2 = p9Months.sumOf { it.shif },
            totalE3 = p9Months.sumOf { it.nssf },
            totalG = p9Months.sumOf { it.interest },
            totalK = p9Months.sumOf { it.chargeablePay },
            totalL = p9Months.sumOf { it.taxCharged },
            totalM = p9Months.sumOf { it.personalRelief },
            totalN = p9Months.sumOf { it.insuranceRelief },
            totalO = p9Months.sumOf { it.payeTax }
        )

        KMPLogger.log("P9 TOTAL RESULT: gross=${summary.totalD}, sha=${summary.totalE2}, nssf=${summary.totalE3}, levy=${summary.totalE1}, paye=${summary.totalO}")
        
        return summary
    }
}
