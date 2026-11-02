package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class InventoryAuditEntity(
    var id: String = "",
    var productId: String = "",
    var beforeQuantity: Double = 0.0, 
    var afterQuantity: Double = 0.0,  
    var changeQuantity: Double = 0.0, 
    var userId: String = "",
    var reason: String = "",
    var timestamp: Long = getCurrentTimestamp()
)

@Serializable
data class StockAdjustmentEntity(
    var id: String = "",
    var productId: String = "",
    var quantity: Double = 0.0, 
    var reason: String = "", 
    var userId: String = "",
    var timestamp: Long = getCurrentTimestamp()
)
