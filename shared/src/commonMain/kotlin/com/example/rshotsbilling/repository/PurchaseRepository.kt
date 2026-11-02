package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*
import kotlinx.datetime.Clock

class PurchaseRepository {
    private val db by lazy {
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Repository] PurchaseRepository initialized.")
        FirestoreManager.db
    }
    private fun getPurchasesRef() = FirestoreManager.getPurchasesRef()

    suspend fun addPurchase(purchase: Purchase) {
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Repository] addPurchase started.")
        SyncStatus.setSyncing(true)
        try {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val id = if (purchase.id.isEmpty() || purchase.id == "pur_0") "pur_$now" else purchase.id
            val newPurchase = purchase.copy(id = id, timestamp = now)
            getPurchasesRef().document(id).set(Purchase.serializer(), newPurchase)
        } finally {
            SyncStatus.setSyncing(false)
        }
    }

    fun getPurchasesFlow(): Flow<List<Purchase>> {
        val path = "companies/${FirestoreManager.getCompanyId()}/purchases"
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Query] Collection: $path | User: ${com.example.rshotsbilling.utils.UserSession.userEmail}")
        return getPurchasesRef().snapshots().map { snapshot ->
            snapshot.documents.map { it.data(Purchase.serializer()) }
        }
    }

    suspend fun getPurchases(): List<Purchase> {
        val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
        val path = if (FirestoreManager.getCompanyId() == "root") "/purchases" else "companies/${FirestoreManager.getCompanyId()}/purchases"
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Purchases query START. Path: $path")
        
        return try {
            val snapshot = getPurchasesRef().get()
            val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
            val count = snapshot.documents.size
            com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Purchases query END: $duration ms. Documents: $count")
            
            snapshot.documents.map {
                it.data(Purchase.serializer())
            }
        } catch (e: Exception) {
            com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Purchases query FAILED: ${e.message}")
            emptyList()
        }
    }

    suspend fun clearPurchasesInRange(from: Long, to: Long) {
        val snapshot = getPurchasesRef().get()
        snapshot.documents.forEach { doc ->
            val purchase = doc.data(Purchase.serializer())
            if (purchase.timestamp in from..to) {
                doc.reference.delete()
            }
        }
    }

    suspend fun updatePurchaseStatus(id: String, status: String, reason: String) {
        getPurchasesRef().document(id).update(
            "status" to status,
            "auditReason" to reason,
            "isApproved" to (status == "APPROVED")
        )
    }

    suspend fun approvePurchase(id: String) {
        updatePurchaseStatus(id, "APPROVED", "System Verified")
    }

    suspend fun deletePurchase(id: String) {
        getPurchasesRef().document(id).delete()
    }
}
