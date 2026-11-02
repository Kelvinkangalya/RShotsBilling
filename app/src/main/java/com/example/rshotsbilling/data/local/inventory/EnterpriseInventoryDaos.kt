package com.example.rshotsbilling.data.local.inventory

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EnterpriseInventoryDao {

    // --- STOCK ADJUSTMENTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: EnterpriseStockAdjustmentEntity)

    @Query("SELECT * FROM enterprise_stock_adjustments WHERE productId = :productId ORDER BY timestamp DESC")
    fun getAdjustmentsForProduct(productId: String): Flow<List<EnterpriseStockAdjustmentEntity>>

    // --- AUDIT TRAIL ---
    @Insert
    suspend fun insertAudit(audit: EnterpriseInventoryAuditEntity)

    @Query("SELECT * FROM enterprise_inventory_audits WHERE productId = :productId ORDER BY timestamp DESC")
    fun getAuditsForProduct(productId: String): Flow<List<EnterpriseInventoryAuditEntity>>

    // --- SUPPLIER ANALYTICS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupplierAnalytics(analytics: EnterpriseSupplierAnalyticsEntity)

    @Query("SELECT * FROM enterprise_supplier_analytics WHERE supplierId = :supplierId")
    suspend fun getSupplierAnalytics(supplierId: String): EnterpriseSupplierAnalyticsEntity?

    @Query("SELECT * FROM enterprise_supplier_analytics")
    fun getAllSupplierAnalytics(): Flow<List<EnterpriseSupplierAnalyticsEntity>>

    // --- ADVANCED QUERIES FOR FIFO & ANALYTICS ---
    
    @Query("""
        SELECT SUM(remainingQuantity * costPrice) 
        FROM inventory_batches 
        WHERE remainingQuantity > 0
    """)
    fun getTotalInventoryValue(): Flow<Double?>

    @Query("""
        SELECT COUNT(id) 
        FROM inventory_batches 
        WHERE expiryDate <= :threshold AND remainingQuantity > 0
    """)
    fun getExpiredBatchesCount(threshold: Long): Flow<Int>
}
