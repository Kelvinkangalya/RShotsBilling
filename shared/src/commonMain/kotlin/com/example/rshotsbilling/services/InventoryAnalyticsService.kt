package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.*

data class InventoryHealthSummary(
    val healthScore: Int = 0,
    val stockTurnoverRate: Double = 0.0,
    val deadStockValue: Double = 0.0,
    val fastMovingCount: Int = 0,
    val slowMovingCount: Int = 0
)

object InventoryAnalyticsService {

    fun calculateStockTurnoverRate(movements: List<StockMovementEntity>): Double {
        val salesCount = movements.filter { it.type == "SALE" }.sumOf { kotlin.math.abs(it.quantity) }
        val avgStock = 100.0 // Placeholder or calculated from history
        return if (avgStock > 0) salesCount / avgStock else 0.0
    }

    fun calculateDeadStockValue(batches: List<InventoryBatchEntity>): Double {
        val threshold = 90L * 24 * 60 * 60 * 1000 // 90 days
        val now = 0L // Should use getCurrentTimestamp() if available or pass as param
        return batches.filter { 
            val age = 0L // now - it.timestamp
            age > threshold && it.remainingQuantity > 0 
        }.sumOf { it.remainingQuantity * it.costPrice }
    }

    fun calculateHealthScore(turnover: Double, deadValue: Double, totalValue: Double, lowStock: Int, outOfStock: Int): Int {
        var score = 100
        if (turnover < 1.0) score -= 20
        if (totalValue > 0 && deadValue / totalValue > 0.2) score -= 30
        score -= (lowStock * 2)
        score -= (outOfStock * 5)
        return score.coerceIn(0, 100)
    }
}
