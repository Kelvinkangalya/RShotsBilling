package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.Receipt
import com.example.rshotsbilling.utils.FirestoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*
import kotlinx.datetime.Clock

class ReceiptRepository {
    private val db by lazy {
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Repository] ReceiptRepository initialized.")
        FirestoreManager.db
    }
    private val receiptsRef get() = FirestoreManager.getReceiptsRef()

    suspend fun addReceipt(receipt: Receipt) {
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Repository] addReceipt started.")
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val id = if (receipt.id.isEmpty()) "rec_$now" else receipt.id
        val newReceipt = receipt.copy(id = id, date = now)
        println("DEBUG: [LOGIN] Attempting Firestore path: receipts/$id")
        receiptsRef.document(id).set(Receipt.serializer(), newReceipt)
    }

    fun getReceiptsFlow(): Flow<List<Receipt>> {
        val path = "companies/${FirestoreManager.getCompanyId()}/receipts"
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Query] Collection: $path | User: ${com.example.rshotsbilling.utils.UserSession.userEmail}")
        return receiptsRef.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(Receipt.serializer()) }
        }
    }

    suspend fun getReceipts(): List<Receipt> {
        val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
        val path = if (FirestoreManager.getCompanyId() == "root") "/receipts" else "companies/${FirestoreManager.getCompanyId()}/receipts"
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Receipts query START. Path: $path")
        
        val snapshot = receiptsRef.get()
        val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
        val count = snapshot.documents.size
        
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Receipts query END: $duration ms. Documents: $count")

        return snapshot.documents.map {
            it.data(Receipt.serializer())
        }
    }

    suspend fun deleteReceipt(id: String) {
        if (id.isEmpty()) return
        println("DEBUG: [LOGIN] Attempting Firestore path: receipts/$id")
        receiptsRef.document(id).delete()
    }

    suspend fun deleteReceiptsByInvoice(invoiceNumber: String) {
        val snapshot = receiptsRef.where { "invoiceNumber" equalTo invoiceNumber }.get()
        snapshot.documents.forEach { doc ->
            doc.reference.delete()
        }
    }
}
