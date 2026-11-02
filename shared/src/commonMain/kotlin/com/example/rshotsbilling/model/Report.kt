package com.example.rshotsbilling.model

data class FinancialSummary(
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val profit: Double = 0.0,
    // eTIMS Upgrades
    val etimsSales: Double = 0.0,
    val nonEtimsSales: Double = 0.0,
    val totalTaxCollected: Double = 0.0,
    val vatCollected: Double = 0.0,
    val pendingSubmissions: Int = 0,
    val failedSubmissions: Int = 0
)
