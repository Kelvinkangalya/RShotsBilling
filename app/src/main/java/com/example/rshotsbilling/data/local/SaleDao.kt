package com.example.rshotsbilling.data.local

import androidx.room.*
import com.example.rshotsbilling.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleOnly(sale: LocalSale)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<LocalSaleItemEntity>)

    @Transaction
    suspend fun insertSale(sale: LocalSale) {
        insertSaleOnly(sale)
        val entities = sale.items.map {
            LocalSaleItemEntity(
                saleId = sale.id,
                serviceId = it.serviceId,
                serviceName = it.serviceName,
                quantity = it.quantity,
                price = it.price
            )
        }
        if (entities.isNotEmpty()) {
            insertSaleItems(entities)
        }
    }

    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleWithItems?

    @Transaction
    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber")
    suspend fun getSaleByInvoiceNumber(invoiceNumber: String): SaleWithItems?

    @Transaction
    @Query("SELECT * FROM sales WHERE etimsStatus = 'PENDING' OR etimsStatus = 'FAILED'")
    suspend fun getPendingEtimsSales(): List<SaleWithItems>

    @Update
    suspend fun updateSale(sale: LocalSale)

    @Delete
    suspend fun deleteSale(sale: LocalSale)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}
