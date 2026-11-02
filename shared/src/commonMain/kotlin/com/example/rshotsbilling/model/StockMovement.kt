package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class StockMovementEntity(
    var id: String = "",
    var productId: String = "",
    var batchId: String? = null,
    var quantity: Double = 0.0, 
    var type: String = "", 
    var reason: String = "",
    var userId: String = "", 
    var timestamp: Long = getCurrentTimestamp(),
    var beforeQuantity: Double = 0.0, 
    var afterQuantity: Double = 0.0,
    var referenceId: String = ""
)
