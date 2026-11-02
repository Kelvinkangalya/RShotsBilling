package com.example.rshotsbilling.data.local

import androidx.room.*
import com.example.rshotsbilling.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- STOCK BATCHES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: LocalInventoryBatchEntity)

    @Update
    suspend fun updateBatch(batch: LocalInventoryBatchEntity)

    @Query("SELECT * FROM inventory_batches WHERE productId = :productId AND remainingQuantity > 0 ORDER BY timestamp ASC")
    suspend fun getAvailableBatchesForProductFIFO(productId: String): List<LocalInventoryBatchEntity>

    @Query("SELECT * FROM inventory_batches WHERE expiryDate <= :threshold")
    fun getExpiringSoonBatches(threshold: Long): Flow<List<LocalInventoryBatchEntity>>

    @Query("SELECT * FROM inventory_batches WHERE productId = :productId")
    fun getBatchesForProduct(productId: String): Flow<List<LocalInventoryBatchEntity>>

    // --- STOCK MOVEMENTS ---
    @Insert
    suspend fun insertMovement(movement: LocalStockMovementEntity)

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: String): Flow<List<LocalStockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getMovementsInRange(start: Long, end: Long): Flow<List<LocalStockMovementEntity>>
    
    @Query("SELECT * FROM stock_movements WHERE productId = :productId AND timestamp BETWEEN :start AND :end")
    suspend fun getMovementsForProductInRange(productId: String, start: Long, end: Long): List<LocalStockMovementEntity>

    // --- SUPPLIERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: LocalSupplierEntity)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<LocalSupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: String): LocalSupplierEntity?

    // --- PURCHASE ORDERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(po: LocalPurchaseOrderEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPOItems(items: List<LocalPurchaseOrderItemEntity>)

    @Query("SELECT * FROM purchase_orders ORDER BY timestamp DESC")
    fun getAllPurchaseOrders(): Flow<List<LocalPurchaseOrderEntity>>

    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    suspend fun getPurchaseOrderById(id: String): LocalPurchaseOrderEntity?
    
    @Query("SELECT * FROM purchase_order_items WHERE poId = :poId")
    suspend fun getPOItems(poId: String): List<LocalPurchaseOrderItemEntity>
    
    @Query("UPDATE purchase_orders SET status = :status WHERE id = :id")
    suspend fun updatePOStatus(id: String, status: String)

    // --- AUDITS & ADJUSTMENTS ---
    @Insert
    suspend fun insertAudit(audit: LocalInventoryAuditEntity)
    
    @Insert
    suspend fun insertAdjustment(adjustment: LocalStockAdjustmentEntity)
}
