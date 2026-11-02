package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.SyncStatus
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.KMPLogger
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*
import kotlinx.datetime.Clock

class SalesRepository(
    private val inventoryRepository: InventoryRepository? = null,
    private val customerRepository: CustomerRepository = CustomerRepository()
) {

    private val db by lazy { 
        KMPLogger.log("DEBUG: [Repository] SalesRepository initialized. User: ${UserSession.userEmail}")
        FirestoreManager.db 
    }
    private fun getSalesRef() = FirestoreManager.getSalesRef()

    suspend fun addSale(sale: Sale): Sale {
        KMPLogger.log("DEBUG: [Repository] addSale started. Company: ${UserSession.companyId}")
        SyncStatus.setSyncing(true)
        try {
            val now = getCurrentTimestamp()
            val id = if (sale.id.isEmpty() || sale.id == "sale_0") "sale_$now" else sale.id
            val newSale = sale.copy(id = id, timestamp = now)
            println("DEBUG: [LOGIN] Attempting Firestore path: sales/$id")
            getSalesRef().document(id).set(Sale.serializer(), newSale)

            // --- AUTOMATIC CUSTOMER MANAGEMENT ---
            if (newSale.customerPhone.isNotEmpty() || newSale.customerName.isNotEmpty()) {
                val existing = customerRepository.findExistingCustomer(
                    phone = newSale.customerPhone,
                    email = "", // Sales model doesn't have customer email yet, but we'll add it if needed
                    name = newSale.customerName
                )
                if (existing != null) {
                    val updated = existing.copy(
                        totalSpending = existing.totalSpending + newSale.total,
                        numberOfPurchases = existing.numberOfPurchases + 1,
                        lastPurchaseDate = newSale.timestamp,
                        updatedAt = now
                    )
                    customerRepository.addOrUpdateCustomer(updated)
                } else if (newSale.customerPhone.isNotEmpty()) {
                    val newCustomer = com.example.rshotsbilling.model.CustomerEntity(
                        name = newSale.customerName,
                        phone = newSale.customerPhone,
                        kraPin = newSale.customerKraPin,
                        totalSpending = newSale.total,
                        numberOfPurchases = 1,
                        lastPurchaseDate = newSale.timestamp,
                        createdAt = now,
                        updatedAt = now
                    )
                    customerRepository.addOrUpdateCustomer(newCustomer)
                }
            }

            // --- DEDUCT STOCK (FIFO) ---
            if (inventoryRepository != null) {
                newSale.items.forEach { item ->
                    inventoryRepository.deductStockFIFO(
                        productId = item.serviceId,
                        quantityToDeduct = item.quantity,
                        referenceId = id,
                        type = "SALE"
                    )
                }
            }

            return newSale
        } finally {
            SyncStatus.setSyncing(false)
        }
    }

    fun getSalesFlow(): Flow<List<Sale>> {
        val path = "companies/${FirestoreManager.getCompanyId()}/sales"
        KMPLogger.log("DEBUG: [Query] Collection: $path | User: ${UserSession.userEmail} | CompanyId: ${UserSession.companyId}")
        return getSalesRef().snapshots().map { snapshot ->
            snapshot.documents.map { it.data(Sale.serializer()) }
        }
    }

    suspend fun getSales(): List<Sale> {
        val startTime = getCurrentTimestamp()
        val path = if (FirestoreManager.getCompanyId() == "root") "/sales" else "companies/${FirestoreManager.getCompanyId()}/sales"
        KMPLogger.log("[PERF] Sales query START. Path: $path")
        
        val snapshot = getSalesRef().get()
        val duration = getCurrentTimestamp() - startTime
        val count = snapshot.documents.size
        
        KMPLogger.log("[PERF] Sales query END: $duration ms. Documents: $count")

        return snapshot.documents.map {
            it.data(Sale.serializer())
        }
    }

    suspend fun deleteSale(id: String) {
        getSalesRef().document(id).delete()
    }

    suspend fun clearSalesInRange(from: Long, to: Long) {
        val snapshot = getSalesRef()
            .where { "timestamp" greaterThanOrEqualTo from }
            .where { "timestamp" lessThanOrEqualTo to }
            .get()
        
        snapshot.documents.forEach { doc ->
            doc.reference.delete()
        }
    }

    suspend fun updateSaleStatus(saleId: String, status: String, reason: String) {
        if (saleId.isEmpty()) return
        println("DEBUG: [LOGIN] Attempting Firestore path: sales/$saleId")
        getSalesRef().document(saleId).update(
            "status" to status,
            "auditReason" to reason,
            "isApproved" to (status == "APPROVED")
        )
    }

    suspend fun approveSale(saleId: String) {
        updateSaleStatus(saleId, "APPROVED", "System Verified")
    }
}
