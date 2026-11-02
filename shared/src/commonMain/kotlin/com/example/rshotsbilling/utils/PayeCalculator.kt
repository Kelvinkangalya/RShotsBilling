package com.example.rshotsbilling.utils

import kotlin.math.max
import kotlin.math.min

/**
 * KRA PAYE Calculator (2024/2025 Standards)
 */
object PayeCalculator {

    data class TaxBand(val limit: Double, val rate: Double)

    private val MONTHLY_PAYE_BANDS = listOf(
        TaxBand(24000.0, 0.10),
        TaxBand(8333.0, 0.25),
        TaxBand(467667.0, 0.30),
        TaxBand(300000.0, 0.325),
        TaxBand(Double.MAX_VALUE, 0.35)
    )

    const val PERSONAL_RELIEF = 2400.0

    /**
     * Calculates PAYE before and after reliefs.
     */
    fun calculate(taxableIncome: Double, shifContribution: Double): PayeResult {
        var remainingIncome = taxableIncome
        var taxCharged = 0.0

        for (band in MONTHLY_PAYE_BANDS) {
            val taxableInThisBand = min(remainingIncome, band.limit)
            taxCharged += taxableInThisBand * band.rate
            remainingIncome -= taxableInThisBand
            if (remainingIncome <= 0) break
        }

        val insuranceRelief = shifContribution * 0.15
        val totalRelief = PERSONAL_RELIEF + insuranceRelief
        val payeTax = max(0.0, taxCharged - totalRelief)

        return PayeResult(
            taxCharged = taxCharged,
            personalRelief = PERSONAL_RELIEF,
            insuranceRelief = insuranceRelief,
            payeTax = payeTax
        )
    }

    data class PayeResult(
        val taxCharged: Double,
        val personalRelief: Double,
        val insuranceRelief: Double,
        val payeTax: Double
    )
}
