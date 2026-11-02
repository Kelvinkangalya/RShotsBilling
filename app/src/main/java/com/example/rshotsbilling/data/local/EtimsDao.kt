package com.example.rshotsbilling.data.local

import androidx.room.*
import com.example.rshotsbilling.model.LocalEtimsInvoice
import kotlinx.coroutines.flow.Flow

@Dao
interface EtimsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: LocalEtimsInvoice)

    @Update
    suspend fun update(invoice: LocalEtimsInvoice)

    @Delete
    suspend fun delete(invoice: LocalEtimsInvoice)

    @Query("SELECT * FROM etims_invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<LocalEtimsInvoice>>

    @Query("SELECT * FROM etims_invoices WHERE status = :status")
    fun getInvoicesByStatus(status: String): Flow<List<LocalEtimsInvoice>>

    @Query("SELECT * FROM etims_invoices WHERE status = 'Pending' OR status = 'Failed'")
    suspend fun getInvoicesToProcess(): List<LocalEtimsInvoice>

    @Query("SELECT * FROM etims_invoices WHERE id = :id")
    suspend fun getInvoiceById(id: String): LocalEtimsInvoice?

    @Query("SELECT SUM(taxAmount) FROM etims_invoices WHERE status = 'Submitted'")
    fun getTotalTaxCollected(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM etims_invoices WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM etims_invoices")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT SUM(taxAmount) FROM etims_invoices WHERE timestamp >= :startOfDay")
    fun getTodayTaxCollected(startOfDay: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM etims_invoices WHERE status = 'Submitted' AND timestamp >= :startOfDay")
    fun getTodaySubmissionsCount(startOfDay: Long): Flow<Int>
}
