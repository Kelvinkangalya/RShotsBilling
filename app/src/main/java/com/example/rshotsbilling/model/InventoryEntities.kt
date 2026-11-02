package com.example.rshotsbilling.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_batches")
data class LocalInventoryBatchEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val batchNumber: String,
    val batchNo: String = "",
    val remainingQuantity: Double,
    val costPrice: Double = 0.0,
    val expiryDate: Long,
    val timestamp: Long
)

@Entity(tableName = "stock_movements")
data class LocalStockMovementEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val quantity: Double,
    val type: String,
    val timestamp: Long
)

@Entity(tableName = "purchase_orders")
data class LocalPurchaseOrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val supplierId: String,
    val status: String,
    val totalAmount: Double,
    val timestamp: Long
)

@Entity(tableName = "purchase_order_items")
data class LocalPurchaseOrderItemEntity(
    @PrimaryKey val id: String,
    val poId: String,
    val productId: String,
    val quantity: Double,
    val price: Double
)

@Entity(tableName = "inventory_audits")
data class LocalInventoryAuditEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val beforeQuantity: Double,
    val afterQuantity: Double,
    val timestamp: Long
)

@Entity(tableName = "stock_adjustments")
data class LocalStockAdjustmentEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val adjustment: Double,
    val reason: String,
    val timestamp: Long
)
