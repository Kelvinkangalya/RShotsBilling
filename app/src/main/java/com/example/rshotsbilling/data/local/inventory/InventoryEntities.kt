package com.example.rshotsbilling.data.local.inventory

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "new_inventory_stock_movements",
    indices = [Index(value = ["productId"]), Index(value = ["movementType"])]
)
data class StockMovementEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val quantity: Double, // Changed to Double
    val movementType: MovementType,
    val userId: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "new_inventory_batches",
    indices = [Index(value = ["productId"]), Index(value = ["batchNumber"], unique = true)]
)
data class InventoryBatchEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val supplierId: String,
    val batchNumber: String,
    val quantityPurchased: Double, // Changed to Double
    val remainingQuantity: Double, // Changed to Double
    val costPrice: Double,
    val manufacturingDate: Long? = null,
    val expiryDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "new_inventory_stock_adjustments",
    indices = [Index(value = ["productId"])]
)
data class StockAdjustmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val previousQuantity: Double, // Changed to Double
    val newQuantity: Double, // Changed to Double
    val adjustmentAmount: Double, // Changed to Double
    val reason: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "new_inventory_audits",
    indices = [Index(value = ["productId"]), Index(value = ["createdAt"])]
)
data class InventoryAuditEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val userId: String,
    val action: String,
    val beforeValue: String,
    val afterValue: String,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "new_inventory_purchase_orders",
    indices = [Index(value = ["orderNumber"], unique = true), Index(value = ["supplierId"])]
)
data class PurchaseOrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val supplierId: String,
    val orderNumber: String,
    val status: PurchaseOrderStatus = PurchaseOrderStatus.REQUESTED,
    val totalAmount: Double = 0.0,
    val requestedBy: String,
    val approvedBy: String? = null,
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val receivedAt: Long? = null
)

@Entity(
    tableName = "new_inventory_purchase_order_items",
    indices = [Index(value = ["purchaseOrderId"]), Index(value = ["productId"])],
    foreignKeys = [
        ForeignKey(
            entity = PurchaseOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseOrderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PurchaseOrderItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val purchaseOrderId: String,
    val productId: String,
    val quantity: Double, // Changed to Double
    val unitCost: Double,
    val totalCost: Double = quantity * unitCost
)

@Entity(tableName = "new_inventory_supplier_analytics")
data class SupplierAnalyticsEntity(
    @PrimaryKey
    val supplierId: String,
    val totalSpent: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val lastPurchaseDate: Long? = null
)
