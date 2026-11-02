package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.StockMovementEntity

/**
 * Summary of a product's stock position for a period.
 */
data class AndroidLedgerSummary(
    val productId: String,
    val openingStock: Double,
    val purchases: Double,
    val sales: Double,
    val returns: Double,
    val damages: Double,
    val expired: Double,
    val adjustments: Double,
    val closingStock: Double
)

/**
 * Service to generate enterprise stock ledgers.
 */
object AndroidStockLedgerService {

    /**
     * Calculates the ledger for a specific product and date range.
     */
    fun calculateProductLedger(
        productId: String,
        movements: List<StockMovementEntity>,
        initialStockAtStart: Double
    ): AndroidLedgerSummary {
        var purchases = 0.0
        var sales = 0.0
        var returns = 0.0
        var damages = 0.0
        var expired = 0.0
        var adjustments = 0.0

        for (m in movements) {
            when (m.type) {
                "PURCHASE" -> purchases += m.quantity
                "SALE" -> sales += Math.abs(m.quantity)
                "RETURN_IN" -> returns += m.quantity
                "RETURN_OUT" -> returns -= Math.abs(m.quantity)
                "DAMAGE" -> damages += Math.abs(m.quantity)
                "EXPIRED" -> expired += Math.abs(m.quantity)
                "ADJUSTMENT" -> adjustments += m.quantity
            }
        }

        val closingStock = initialStockAtStart + purchases - sales + returns - damages - expired + adjustments

        return AndroidLedgerSummary(
            productId = productId,
            openingStock = initialStockAtStart,
            purchases = purchases,
            sales = sales,
            returns = returns,
            damages = damages,
            expired = expired,
            adjustments = adjustments,
            closingStock = closingStock
        )
    }
}
