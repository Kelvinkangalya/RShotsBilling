package com.example.rshotsbilling.data.local

import androidx.room.*
import com.example.rshotsbilling.model.LocalMpesaTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MpesaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: LocalMpesaTransaction)

    @Update
    suspend fun update(transaction: LocalMpesaTransaction)

    @Query("SELECT * FROM mpesa_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<LocalMpesaTransaction>>

    @Query("SELECT * FROM mpesa_transactions WHERE status = 'PENDING'")
    suspend fun getPendingTransactions(): List<LocalMpesaTransaction>

    @Query("SELECT SUM(amount) FROM mpesa_transactions WHERE status = 'SUCCESS'")
    fun getTotalMpesaRevenue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM mpesa_transactions WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT SUM(amount) FROM mpesa_transactions WHERE status = 'SUCCESS' AND createdAt >= :startOfDay")
    fun getTodayMpesaRevenue(startOfDay: Long): Flow<Double?>
}
