package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseOrderEntity(
    var id: String = "",
    var supplierId: String = "",
    var timestamp: Long = getCurrentTimestamp(),
    var totalAmount: Double = 0.0,
    var status: String = "PENDING" 
)

@Serializable
data class PurchaseOrderItemEntity(
    var id: String = "",
    var poId: String = "",
    var productId: String = "",
    var productName: String = "",
    var quantity: Double = 0.0,
    var unitCost: Double = 0.0
)
