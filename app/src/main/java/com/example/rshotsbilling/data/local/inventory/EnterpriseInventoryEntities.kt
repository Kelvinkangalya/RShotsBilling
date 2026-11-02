package com.example.rshotsbilling.data.local.inventory

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Tracks formal stock adjustments (Damage, Lost, Correction).
 */
@Entity(
    tableName = "enterprise_stock_adjustments",
    indices = [Index(value = ["productId"])]
)
data class EnterpriseStockAdjustmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val previousQuantity: Double,
    val newQuantity: Double,
    val adjustmentAmount: Double,
    val reason: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Immutable audit trail for every single change to inventory.
 */
@Entity(
    tableName = "enterprise_inventory_audits",
    indices = [Index(value = ["productId"]), Index(value = ["timestamp"])]
)
data class EnterpriseInventoryAuditEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val userId: String,
    val action: String, // e.g., "PRICE_CHANGE", "STOCK_SYNC", "BATCH_CREATED"
    val beforeValue: String,
    val afterValue: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Advanced analytics for suppliers.
 */
@Entity(tableName = "enterprise_supplier_analytics")
data class EnterpriseSupplierAnalyticsEntity(
    @PrimaryKey
    val supplierId: String,
    val totalSpent: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val totalPurchasesCount: Int = 0,
    val lastPurchaseDate: Long? = null
)

/**
 * Extended Purchase Order Statuses.
 */
enum class EnterprisePOStatus {
    REQUESTED,
    APPROVED,
    ORDERED,
    RECEIVED,
    CLOSED
}
