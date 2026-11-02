package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.MpesaTransaction
import com.example.rshotsbilling.network.MpesaService
import com.example.rshotsbilling.network.MpesaResponse
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*

class MpesaRepository(private val mpesaService: MpesaService? = null) {
    private val db by lazy {
        println("DEBUG: MpesaRepository initialized.")
        FirestoreManager.db
    }
    private val transactionsRef get() = FirestoreManager.getMpesaTransactionsRef()

    fun getAllTransactionsFlow(): Flow<List<MpesaTransaction>> = transactionsRef.snapshots().map { snapshot ->
        snapshot.documents.map { it.data(MpesaTransaction.serializer()) }
    }

    suspend fun addTransaction(transaction: MpesaTransaction) {
        val t = getCurrentTimestamp()
        val id = if (transaction.id.isEmpty()) "mpesa_$t" else transaction.id
        val newTransaction = transaction.copy(id = id, createdAt = t, updatedAt = t)
        println("DEBUG: [LOGIN] Attempting Firestore path: mpesa_transactions/$id")
        transactionsRef.document(id).set(MpesaTransaction.serializer(), newTransaction)
    }

    suspend fun updateTransaction(transaction: MpesaTransaction) {
        if (transaction.id.isEmpty()) return
        val updated = transaction.copy(updatedAt = getCurrentTimestamp())
        println("DEBUG: [LOGIN] Attempting Firestore path: mpesa_transactions/${updated.id}")
        transactionsRef.document(updated.id).set(MpesaTransaction.serializer(), updated)
    }

    // --- ANALYTICS ---
    fun getCountByStatus(status: String): Flow<Int> = getAllTransactionsFlow().map { list ->
        list.count { it.status == status }
    }

    fun getTotalRevenue(): Flow<Double> = getAllTransactionsFlow().map { list ->
        list.filter { it.status == "SUCCESS" }.sumOf { it.amount }
    }

    fun getTodayRevenue(startOfDay: Long): Flow<Double> = getAllTransactionsFlow().map { list ->
        list.filter { it.createdAt >= startOfDay && it.status == "SUCCESS" }.sumOf { it.amount }
    }

    // --- PAYMENT FLOW ---
    suspend fun initiateStkPush(saleId: String, phone: String, amount: Double): MpesaResponse {
        if (mpesaService == null) return MpesaResponse(false, message = "M-Pesa Service not configured")

        val response = mpesaService.initiatePayment(phone, amount, saleId)
        
        val transaction = MpesaTransaction(
            saleId = saleId,
            phoneNumber = phone,
            amount = amount,
            transactionId = response.transactionId,
            status = if (response.isPending) "PENDING" else if (response.success) "SUCCESS" else "FAILED",
            errorMessage = response.message
        )
        addTransaction(transaction)
        
        return response
    }

    suspend fun checkStatus(transactionId: String) {
        if (mpesaService == null) return

        val response = mpesaService.checkTransactionStatus(transactionId)
        if (response.success) {
            // Find and update the transaction in Firestore
            val snapshot = transactionsRef.where { "transactionId" equalTo transactionId }.get()
            snapshot.documents.firstOrNull()?.let { doc ->
                val transaction = doc.data(MpesaTransaction.serializer())
                updateTransaction(transaction.copy(status = "SUCCESS"))
            }
        }
    }
}
