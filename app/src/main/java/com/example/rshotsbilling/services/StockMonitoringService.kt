package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.Service

/**
 * Stock Alert Levels.
 */
enum class AndroidStockAlertLevel {
    NORMAL,
    LOW_STOCK,
    CRITICAL_STOCK,
    OUT_OF_STOCK
}

/**
 * Service to monitor stock levels against thresholds.
 */
object AndroidStockMonitoringService {

    /**
     * Determines the alert level for a product.
     */
    fun getStockAlertLevel(product: Service, minLevel: Double, reorderLevel: Double): StockAlertLevel {
        val qty = product.stockQuantity
        
        return when {
            qty <= 0 -> StockAlertLevel.OUT_OF_STOCK
            qty <= minLevel -> StockAlertLevel.CRITICAL_STOCK
            qty <= reorderLevel -> StockAlertLevel.LOW_STOCK
            else -> StockAlertLevel.NORMAL
        }
    }
}
