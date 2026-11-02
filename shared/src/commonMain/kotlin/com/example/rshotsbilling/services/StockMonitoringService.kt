package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.Service

enum class StockAlertLevel {
    NORMAL,
    LOW_STOCK,
    CRITICAL_STOCK,
    OUT_OF_STOCK
}

object StockMonitoringService {

    fun getStockAlertLevel(service: Service, lowThreshold: Double = 5.0, criticalThreshold: Double = 2.0): StockAlertLevel {
        val qty = service.stockQuantity
        return when {
            qty <= 0.0 -> StockAlertLevel.OUT_OF_STOCK
            qty <= criticalThreshold -> StockAlertLevel.CRITICAL_STOCK
            qty <= lowThreshold -> StockAlertLevel.LOW_STOCK
            else -> StockAlertLevel.NORMAL
        }
    }
}
