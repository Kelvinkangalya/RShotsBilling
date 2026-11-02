package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.StockMovementEntity
import kotlin.math.abs

data class LedgerSummary(
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

object StockLedgerService {

    fun calculateProductLedger(productId: String, movements: List<StockMovementEntity>, openingStock: Double = 0.0): LedgerSummary {
        var purchases = 0.0
        var sales = 0.0
        var returns = 0.0
        var damages = 0.0
        var expired = 0.0
        var adjustments = 0.0

        movements.forEach { mov ->
            when (mov.type) {
                "PURCHASE" -> purchases += mov.quantity
                "SALE" -> sales += abs(mov.quantity)
                "RETURN_IN" -> returns += mov.quantity
                "RETURN_OUT" -> returns -= abs(mov.quantity)
                "DAMAGE" -> damages += abs(mov.quantity)
                "EXPIRED" -> expired += abs(mov.quantity)
                "ADJUSTMENT" -> adjustments += mov.quantity
            }
        }

        val closing = openingStock + purchases - sales + returns - damages - expired + adjustments

        return LedgerSummary(
            productId = productId,
            openingStock = openingStock,
            purchases = purchases,
            sales = sales,
            returns = returns,
            damages = damages,
            expired = expired,
            adjustments = adjustments,
            closingStock = closing
        )
    }
}
