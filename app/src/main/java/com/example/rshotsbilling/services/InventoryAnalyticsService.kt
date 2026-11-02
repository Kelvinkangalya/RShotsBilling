package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.InventoryBatchEntity
import com.example.rshotsbilling.model.StockMovementEntity
import com.example.rshotsbilling.model.Service

/**
 * Summary of inventory health.
 */
data class AndroidInventoryHealthSummary(
    val healthScore: Int, // 0-100
    val stockTurnoverRate: Double,
    val deadStockValue: Double,
    val fastMovingCount: Int,
    val slowMovingCount: Int
)

/**
 * Service to calculate advanced inventory business intelligence.
 */
object AndroidInventoryAnalyticsService {

    /**
     * Calculates turnover rate: (COGS / Average Inventory).
     * Simplified for this context: (Total Sold Qty / Total Purchases Qty) over period.
     */
    fun calculateStockTurnoverRate(movements: List<StockMovementEntity>): Double {
        val totalPurchased = movements.filter { it.type == "PURCHASE" }.sumOf { it.quantity }
        val totalSold = movements.filter { it.type == "SALE" }.sumOf { Math.abs(it.quantity) }
        
        if (totalPurchased <= 0) return 0.0
        return totalSold / totalPurchased
    }

    /**
     * Identifies "Dead Stock" - batches older than 180 days with remaining qty.
     */
    fun calculateDeadStockValue(batches: List<InventoryBatchEntity>): Double {
        val threshold = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
        return batches
            .filter { it.timestamp < threshold && it.remainingQuantity > 0 }
            .sumOf { it.remainingQuantity * it.costPrice }
    }

    /**
     * Calculates an overall health score for the inventory management.
     */
    fun calculateHealthScore(
        turnoverRate: Double, 
        deadStockValue: Double, 
        totalValue: Double,
        outOfStockCount: Int,
        totalProducts: Int
    ): Int {
        var score = 100
        
        // Penalize dead stock
        if (totalValue > 0) {
            val deadStockRatio = deadStockValue / totalValue
            score -= (deadStockRatio * 30).toInt()
        }
        
        // Penalize out of stock
        if (totalProducts > 0) {
            val oosRatio = outOfStockCount.toDouble() / totalProducts
            score -= (oosRatio * 20).toInt()
        }
        
        // Penalize low turnover
        if (turnoverRate < 0.2) score -= 15
        
        return score.coerceIn(0, 100)
    }
}
