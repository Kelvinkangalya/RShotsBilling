package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class InventoryBatchEntity(
    var id: String = "",
    var productId: String = "",
    var batchNo: String = "",
    var supplierId: String = "",
    var purchaseId: String = "",
    var manufacturingDate: Long? = null,
    var expiryDate: Long? = null,
    var costPrice: Double = 0.0,
    var originalQuantity: Double = 0.0, 
    var remainingQuantity: Double = 0.0, 
    var valuationMethod: String = "FIFO", // FIFO, LIFO, WAC
    var minStock: Double = 5.0,
    var maxStock: Double = 100.0,
    var reorderLevel: Double = 10.0,
    var warehouseLocation: String = "",
    var shelfNumber: String = "",
    var timestamp: Long = getCurrentTimestamp()
)
