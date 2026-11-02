package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.InventoryBatchEntity
import com.example.rshotsbilling.utils.getCurrentTimestamp

enum class ExpiryRisk {
    EXPIRED,
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

object ExpiryManagementService {

    fun getExpiryRisk(batch: InventoryBatchEntity): ExpiryRisk {
        val expiry = batch.expiryDate ?: return ExpiryRisk.LOW
        val now = getCurrentTimestamp()
        val daysRemaining = (expiry - now) / (1000 * 60 * 60 * 24)

        return when {
            daysRemaining <= 0 -> ExpiryRisk.EXPIRED
            daysRemaining <= 7 -> ExpiryRisk.CRITICAL
            daysRemaining <= 30 -> ExpiryRisk.HIGH
            daysRemaining <= 90 -> ExpiryRisk.MEDIUM
            else -> ExpiryRisk.LOW
        }
    }

    fun calculateExpiryRiskValue(batches: List<InventoryBatchEntity>): Double {
        val now = getCurrentTimestamp()
        val criticalThreshold = now + 30L * 24 * 60 * 60 * 1000
        return batches.filter { (it.expiryDate ?: 0) in 1..criticalThreshold }
            .sumOf { it.remainingQuantity * it.costPrice }
    }
}
