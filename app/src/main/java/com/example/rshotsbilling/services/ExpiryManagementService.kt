package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.InventoryBatchEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Expiry Risk categories.
 */
enum class AndroidExpiryRisk {
    EXPIRED,
    CRITICAL, // < 7 days
    HIGH,     // < 30 days
    MEDIUM,   // < 90 days
    LOW       // > 90 days
}

/**
 * Service to manage product expirations and risks.
 */
object AndroidExpiryManagementService {

    /**
     * Categorizes a batch based on its expiry date.
     */
    fun getExpiryRisk(batch: InventoryBatchEntity): ExpiryRisk {
        val expiry = batch.expiryDate ?: return ExpiryRisk.LOW
        val now = System.currentTimeMillis()
        val diff = expiry - now
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            diff <= 0 -> ExpiryRisk.EXPIRED
            days <= 7 -> ExpiryRisk.CRITICAL
            days <= 30 -> ExpiryRisk.HIGH
            days <= 90 -> ExpiryRisk.MEDIUM
            else -> ExpiryRisk.LOW
        }
    }

    /**
     * Calculates the financial value at risk of expiry.
     */
    fun calculateExpiryRiskValue(batches: List<InventoryBatchEntity>): Double {
        return batches
            .filter { getExpiryRisk(it) != ExpiryRisk.LOW }
            .sumOf { it.remainingQuantity * it.costPrice }
    }
}
