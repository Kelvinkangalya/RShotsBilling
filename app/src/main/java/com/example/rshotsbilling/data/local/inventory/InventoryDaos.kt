package com.example.rshotsbilling.data.local.inventory

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovementEntity>)

    @Query("SELECT * FROM new_inventory_stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun getMovementsForProduct(productId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM new_inventory_stock_movements WHERE movementType = :type ORDER BY createdAt DESC")
    fun getMovementsByType(type: MovementType): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM new_inventory_stock_movements WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun getMovementsBetweenDates(start: Long, end: Long): Flow<List<StockMovementEntity>>
}

@Dao
interface InventoryBatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: InventoryBatchEntity)

    @Update
    suspend fun updateBatch(batch: InventoryBatchEntity)

    @Query("SELECT * FROM new_inventory_batches WHERE productId = :productId ORDER BY expiryDate ASC")
    fun getBatchesForProduct(productId: String): Flow<List<InventoryBatchEntity>>

    @Query("SELECT * FROM new_inventory_batches WHERE productId = :productId AND remainingQuantity > 0 ORDER BY createdAt ASC")
    suspend fun getAvailableBatchesFIFO(productId: String): List<InventoryBatchEntity>

    @Query("SELECT * FROM new_inventory_batches WHERE expiryDate < :currentTime")
    fun getExpiredBatches(currentTime: Long = System.currentTimeMillis()): Flow<List<InventoryBatchEntity>>

    @Query("SELECT * FROM new_inventory_batches WHERE expiryDate BETWEEN :currentTime AND :futureTime")
    fun getExpiringSoon(currentTime: Long, futureTime: Long): Flow<List<InventoryBatchEntity>>
}

@Dao
interface StockAdjustmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: StockAdjustmentEntity)

    @Query("SELECT * FROM new_inventory_stock_adjustments WHERE productId = :productId ORDER BY createdAt DESC")
    fun getAdjustmentsForProduct(productId: String): Flow<List<StockAdjustmentEntity>>
}

@Dao
interface InventoryAuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: InventoryAuditEntity)

    @Query("SELECT * FROM new_inventory_audits WHERE productId = :productId ORDER BY createdAt DESC")
    fun getAuditTrail(productId: String): Flow<List<InventoryAuditEntity>>

    @Query("SELECT * FROM new_inventory_audits WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun getAuditTrailBetweenDates(start: Long, end: Long): Flow<List<InventoryAuditEntity>>
}

@Dao
interface PurchaseOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(po: PurchaseOrderEntity)

    @Update
    suspend fun updatePurchaseOrder(po: PurchaseOrderEntity)

    @Query("SELECT * FROM new_inventory_purchase_orders WHERE id = :id")
    suspend fun getPurchaseOrder(id: String): PurchaseOrderEntity?

    @Query("SELECT * FROM new_inventory_purchase_orders WHERE status = :status ORDER BY requestedAt DESC")
    fun getPurchaseOrdersByStatus(status: PurchaseOrderStatus): Flow<List<PurchaseOrderEntity>>
}

@Dao
interface PurchaseOrderItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseOrderItemEntity>)

    @Query("SELECT * FROM new_inventory_purchase_order_items WHERE purchaseOrderId = :orderId")
    suspend fun getItemsForOrder(orderId: String): List<PurchaseOrderItemEntity>
}

@Dao
interface SupplierAnalyticsDao {
    @Upsert
    suspend fun upsertAnalytics(analytics: SupplierAnalyticsEntity)

    @Query("SELECT * FROM new_inventory_supplier_analytics WHERE supplierId = :supplierId")
    suspend fun getSupplierAnalytics(supplierId: String): SupplierAnalyticsEntity?
}
