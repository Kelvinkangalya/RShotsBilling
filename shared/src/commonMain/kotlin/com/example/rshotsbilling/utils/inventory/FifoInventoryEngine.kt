package com.example.rshotsbilling.utils.inventory

import com.example.rshotsbilling.model.InventoryBatchEntity

/**
 * Result of a FIFO stock consumption operation.
 */
data class FifoConsumptionResult(
    val totalQuantityConsumed: Double,
    val totalCOGS: Double,
    val batchConsumptions: List<BatchConsumption>,
    val updatedBatches: List<InventoryBatchEntity>
)

/**
 * Detailed breakdown of consumption from a specific batch.
 */
data class BatchConsumption(
    val batchId: String,
    val batchNumber: String,
    val quantityTaken: Double,
    val unitCost: Double,
    val totalCost: Double = quantityTaken * unitCost
)

/**
 * Core engine for FIFO (First-In, First-Out) inventory accounting.
 */
object FifoInventoryEngine {

    /**
     * Consumes stock from a list of batches using FIFO logic.
     * @param batches List of available batches, should be pre-sorted by date.
     * @param quantityToConsume Total quantity to deduct.
     * @return FifoConsumptionResult containing updated batches and COGS data.
     */
    fun consumeBatches(
        batches: List<InventoryBatchEntity>,
        quantityToConsume: Double
    ): FifoConsumptionResult {
        var remainingNeeded = quantityToConsume
        var totalCOGS = 0.0
        val consumptionBreakdown = mutableListOf<BatchConsumption>()
        val modifiedBatches = mutableListOf<InventoryBatchEntity>()

        // FIFO: Oldest batches first
        val sortedBatches = batches.sortedBy { it.timestamp }

        for (batch in sortedBatches) {
            if (remainingNeeded <= 0) break
            if (batch.remainingQuantity <= 0) continue

            val takenFromThisBatch = minOf(batch.remainingQuantity, remainingNeeded)
            val batchCOGS = takenFromThisBatch * batch.costPrice
            
            totalCOGS += batchCOGS
            remainingNeeded -= takenFromThisBatch

            consumptionBreakdown.add(
                BatchConsumption(
                    batchId = batch.id,
                    batchNumber = batch.batchNo,
                    quantityTaken = takenFromThisBatch,
                    unitCost = batch.costPrice
                )
            )

            modifiedBatches.add(
                batch.copy(
                    remainingQuantity = batch.remainingQuantity - takenFromThisBatch
                )
            )
        }

        return FifoConsumptionResult(
            totalQuantityConsumed = quantityToConsume - remainingNeeded,
            totalCOGS = totalCOGS,
            batchConsumptions = consumptionBreakdown,
            updatedBatches = modifiedBatches
        )
    }

    /**
     * Calculates the total value of remaining inventory.
     */
    fun calculateRemainingInventoryValue(batches: List<InventoryBatchEntity>): Double {
        return batches.sumOf { it.remainingQuantity * it.costPrice }
    }
}
