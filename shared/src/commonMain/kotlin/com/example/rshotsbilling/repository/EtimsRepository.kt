package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.EtimsInvoice
import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.network.EtimsService
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*

class EtimsRepository(private val etimsService: EtimsService? = null) {
    private val db by lazy {
        println("DEBUG: EtimsRepository initialized.")
        FirestoreManager.db
    }
    private val etimsRef get() = FirestoreManager.getEtimsInvoicesRef()
    private val salesRef get() = FirestoreManager.getSalesRef()

    fun getAllInvoicesFlow(): Flow<List<EtimsInvoice>> = etimsRef.snapshots().map { snapshot ->
        snapshot.documents.map { it.data(EtimsInvoice.serializer()) }
    }

    suspend fun addInvoice(invoice: EtimsInvoice) {
        val t = getCurrentTimestamp()
        val id = if (invoice.id.isEmpty()) "etims_$t" else invoice.id
        val newInvoice = invoice.copy(id = id, timestamp = t)
        println("DEBUG: [LOGIN] Attempting Firestore path: etims_invoices/$id")
        etimsRef.document(id).set(EtimsInvoice.serializer(), newInvoice)
    }

    suspend fun updateInvoice(invoice: EtimsInvoice) {
        if (invoice.id.isEmpty()) return
        println("DEBUG: [LOGIN] Attempting Firestore path: etims_invoices/${invoice.id}")
        etimsRef.document(invoice.id).set(EtimsInvoice.serializer(), invoice)
    }

    suspend fun deleteInvoice(id: String) {
        etimsRef.document(id).delete()
    }

    // --- ANALYTICS ---
    fun getCountByStatus(status: String): Flow<Int> = getAllInvoicesFlow().map { list ->
        list.count { it.status == status }
    }

    fun getTotalTaxCollected(): Flow<Double> = getAllInvoicesFlow().map { list ->
        list.sumOf { it.taxAmount }
    }

    fun getTodayTaxCollected(startOfDay: Long): Flow<Double> = getAllInvoicesFlow().map { list ->
        list.filter { it.timestamp >= startOfDay }.sumOf { it.taxAmount }
    }

    fun getTodaySubmissionsCount(startOfDay: Long): Flow<Int> = getAllInvoicesFlow().map { list ->
        list.count { it.timestamp >= startOfDay && it.status == "Submitted" }
    }

    // --- SUBMISSION LOGIC ---
    suspend fun submitSaleToEtims(sale: Sale): Sale {
        if (etimsService == null) return sale

        return try {
            val response = etimsService.submitInvoice(sale)
            if (response.success) {
                val updatedSale = sale.copy(
                    etimsStatus = "SUCCESS",
                    etimsReceiptNumber = response.receiptNumber ?: "",
                    etimsQrData = response.qrData ?: "",
                    etimsSubmissionDate = getCurrentTimestamp()
                )
                println("DEBUG: [LOGIN] Attempting Firestore path: sales/${updatedSale.id}")
                salesRef.document(updatedSale.id).set(Sale.serializer(), updatedSale)
                
                // Also update the EtimsInvoice record if it exists
                val invoiceSnapshot = etimsRef.where { "saleId" equalTo sale.id }.get()
                invoiceSnapshot.documents.firstOrNull()?.let { doc ->
                    val invoice = doc.data(EtimsInvoice.serializer())
                    updateInvoice(invoice.copy(status = "Submitted"))
                }

                updatedSale
            } else {
                val failedSale = sale.copy(etimsStatus = "FAILED")
                println("DEBUG: [LOGIN] Attempting Firestore path: sales/${failedSale.id}")
                salesRef.document(failedSale.id).set(Sale.serializer(), failedSale)
                failedSale
            }
        } catch (e: Exception) {
            val failedSale = sale.copy(etimsStatus = "FAILED")
            println("DEBUG: [LOGIN] Attempting Firestore path: sales/${failedSale.id}")
            salesRef.document(failedSale.id).set(Sale.serializer(), failedSale)
            failedSale
        }
    }

    suspend fun getPendingSales(): List<Sale> {
        val snapshot = salesRef
            .where { "etimsStatus" inArray listOf("PENDING", "FAILED") }
            .get()
        return snapshot.documents.map { it.data(Sale.serializer()) }
    }

    suspend fun resubmitPendingSales() {
        val pending = getPendingSales()
        pending.forEach { submitSaleToEtims(it) }
    }
}
