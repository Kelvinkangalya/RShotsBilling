package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.*
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.SyncStatus
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.getCurrentTimestamp
import com.example.rshotsbilling.utils.KMPLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*
import kotlinx.datetime.Clock
import kotlin.math.abs

class InventoryRepository {

    private val db: FirebaseFirestore by lazy {
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Repository] InventoryRepository initialized.")
        FirestoreManager.db
    }
    
    private fun getServicesRef() = FirestoreManager.getProductsRef()
    private fun getSuppliersRef() = FirestoreManager.getCompanyRef().collection("suppliers")
    private fun getBatchesRef() = FirestoreManager.getCompanyRef().collection("stock_batches")
    private fun getMovementsRef() = FirestoreManager.getCompanyRef().collection("stock_movements")
    private fun getPoRef() = FirestoreManager.getCompanyRef().collection("purchase_orders")
    private fun getAuditRef() = FirestoreManager.getCompanyRef().collection("inventory_audits")
    private fun getAdjustmentRef() = FirestoreManager.getCompanyRef().collection("stock_adjustments")

    private fun now(): Long = getCurrentTimestamp()

    // --- STOCK DEDUCTION (FIFO) ---
    suspend fun deductStockFIFO(productId: String, quantityToDeduct: Double, referenceId: String, type: String) {
        try {
            // Get available batches sorted by timestamp ASC (FIFO)
            val batchesSnapshot = getBatchesRef()
                .where { "productId" equalTo productId }
                .where { "remainingQuantity" greaterThan 0.0 }
                .get()
            
            val availableBatches = batchesSnapshot.documents
                .map { it.data(InventoryBatchEntity.serializer()) }
                .sortedBy { it.timestamp }

            var remainingToDeduct = quantityToDeduct

            // Record Initial Quantity for Audit
            val snapshot = getServicesRef().document(productId).get()
            val service = snapshot.data(Service.serializer())
            val beforeQty = service.stockQuantity

            for (batch in availableBatches) {
                if (remainingToDeduct <= 0.0) break

                val deductionFromThisBatch = minOf(batch.remainingQuantity, remainingToDeduct)
                val updatedBatch = batch.copy(remainingQuantity = batch.remainingQuantity - deductionFromThisBatch)
                
                println("DEBUG: [LOGIN] Attempting Firestore path: stock_batches/${updatedBatch.id}")
                getBatchesRef().document(updatedBatch.id).set(InventoryBatchEntity.serializer(), updatedBatch)

                logMovement(
                    productId = productId,
                    batchId = batch.id,
                    quantity = -deductionFromThisBatch,
                    type = type,
                    reason = "FIFO deduction for $referenceId",
                    referenceId = referenceId,
                    beforeQty = batch.remainingQuantity,
                    afterQty = updatedBatch.remainingQuantity
                )

                remainingToDeduct -= deductionFromThisBatch
            }
            
            updateServiceStock(productId, beforeQty, "Stock deduction: $type")
        } catch (e: Exception) {
            // Log error or handle
        }
    }

    private suspend fun updateServiceStock(productId: String, beforeQty: Double? = null, reason: String = "Auto sync") {
        try {
            val snapshot = getServicesRef().document(productId).get()
            val service = snapshot.data(Service.serializer())
            
            val batchesSnapshot = getBatchesRef()
                .where { "productId" equalTo productId }
                .where { "remainingQuantity" greaterThan 0.0 }
                .get()
            
            val totalStock = batchesSnapshot.documents
                .map { it.data(InventoryBatchEntity.serializer()) }
                .sumOf { it.remainingQuantity }
            
            val updatedService = service.copy(stockQuantity = totalStock)
            println("DEBUG: [LOGIN] Attempting Firestore path: products/$productId")
            getServicesRef().document(productId).set(Service.serializer(), updatedService)

            if (beforeQty != null && beforeQty != totalStock) {
                logAudit(productId, beforeQty, totalStock, reason)
            }
        } catch (e: Exception) {
        }
    }

    // --- MOVEMENTS & AUDITS ---
    suspend fun logMovement(
        productId: String,
        batchId: String? = null,
        quantity: Double,
        type: String,
        reason: String,
        referenceId: String,
        beforeQty: Double = 0.0,
        afterQty: Double = 0.0
    ) {
        val t = now()
        val movement = StockMovementEntity(
            id = "mov_$t",
            productId = productId,
            batchId = batchId,
            quantity = quantity,
            type = type,
            reason = reason,
            userId = UserSession.userEmail ?: "System",
            timestamp = t,
            beforeQuantity = beforeQty,
            afterQuantity = afterQty,
            referenceId = referenceId
        )
        println("DEBUG: [LOGIN] Attempting Firestore path: stock_movements/${movement.id}")
        getMovementsRef().document(movement.id).set(StockMovementEntity.serializer(), movement)
    }

    suspend fun logAudit(productId: String, before: Double, after: Double, reason: String) {
        val t = now()
        val audit = InventoryAuditEntity(
            id = "audit_$t",
            productId = productId,
            beforeQuantity = before,
            afterQuantity = after,
            changeQuantity = after - before,
            userId = UserSession.userEmail ?: "System",
            reason = reason,
            timestamp = t
        )
        println("DEBUG: [LOGIN] Attempting Firestore path: inventory_audits/${audit.id}")
        getAuditRef().document(audit.id).set(InventoryAuditEntity.serializer(), audit)
    }

    suspend fun logAdjustment(productId: String, quantity: Double, type: String, reason: String) {
        val t = now()
        val id = "adj_$t"
        val adjustment = StockAdjustmentEntity(
            id = id,
            productId = productId,
            quantity = quantity,
            reason = type,
            userId = UserSession.userEmail ?: "System",
            timestamp = t
        )
        println("DEBUG: [LOGIN] Attempting Firestore path: stock_adjustments/$id")
        getAdjustmentRef().document(id).set(StockAdjustmentEntity.serializer(), adjustment)
        
        // Also log as movement
        logMovement(
            productId = productId,
            quantity = quantity,
            type = type,
            reason = reason,
            referenceId = id
        )
    }

    // --- BATCHES ---
    suspend fun addStockBatch(batch: InventoryBatchEntity) {
        SyncStatus.setSyncing(true)
        try {
            val t = now()
            val id = if (batch.id.isEmpty()) "batch_$t" else batch.id
            val finalBatch = batch.copy(id = id, timestamp = t)
            
            println("DEBUG: [LOGIN] Attempting Firestore path: stock_batches/$id")
            getBatchesRef().document(id).set(InventoryBatchEntity.serializer(), finalBatch)
            
            logMovement(
                productId = finalBatch.productId,
                batchId = id,
                quantity = finalBatch.originalQuantity,
                type = "PURCHASE",
                reason = "New batch: ${finalBatch.batchNo}",
                referenceId = finalBatch.purchaseId,
                beforeQty = 0.0,
                afterQty = finalBatch.originalQuantity
            )
            
            updateServiceStock(finalBatch.productId)
        } finally {
            SyncStatus.setSyncing(false)
        }
    }

    // --- SUPPLIERS ---
    suspend fun addSupplier(supplier: SupplierEntity) {
        SyncStatus.setSyncing(true)
        try {
            val t = now()
            val id = if (supplier.id.isEmpty()) "sup_$t" else supplier.id
            val finalSupplier = supplier.copy(id = id, timestamp = t)
            println("DEBUG: [LOGIN] Attempting Firestore path: suppliers/$id")
            getSuppliersRef().document(id).set(SupplierEntity.serializer(), finalSupplier)
        } finally {
            SyncStatus.setSyncing(false)
        }
    }

    fun getAllSuppliers(): Flow<List<SupplierEntity>> {
        val path = "companies/${FirestoreManager.getCompanyId()}/suppliers"
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Query] Collection: $path | User: ${com.example.rshotsbilling.utils.UserSession.userEmail}")
        return getSuppliersRef().snapshots().map { snapshot ->
            snapshot.documents.map { it.data(SupplierEntity.serializer()) }
        }
    }

    fun getMovementsForProduct(productId: String): Flow<List<StockMovementEntity>> {
        val path = "companies/${FirestoreManager.getCompanyId()}/stock_movements"
        com.example.rshotsbilling.utils.KMPLogger.log("DEBUG: [Query] Collection: $path | Product: $productId")
        return getMovementsRef().where { "productId" equalTo productId }.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(StockMovementEntity.serializer()) }
        }
    }

    fun getBatchesForProduct(productId: String): Flow<List<InventoryBatchEntity>> = 
        getBatchesRef().where { "productId" equalTo productId }.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(InventoryBatchEntity.serializer()) }
        }

    fun getExpiringSoonBatches(threshold: Long): Flow<List<InventoryBatchEntity>> = 
        getBatchesRef().where { "expiryDate" lessThanOrEqualTo threshold }.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(InventoryBatchEntity.serializer()) }
        }

    // --- STOCK VALUATION ---
    suspend fun calculateStockValue(): Double {
        val startTime = getCurrentTimestamp()
        KMPLogger.log("[PERF] Stock valuation query START")
        return try {
            val batchesSnapshot = getBatchesRef().where { "remainingQuantity" greaterThan 0.0 }.get()
            val duration = getCurrentTimestamp() - startTime
            val count = batchesSnapshot.documents.size
            KMPLogger.log("[PERF] Stock valuation query END: $duration ms. Batches: $count")
            
            batchesSnapshot.documents
                .map { it.data(InventoryBatchEntity.serializer()) }
                .sumOf { it.remainingQuantity * it.costPrice }
        } catch (e: Exception) {
            KMPLogger.log("[PERF] Stock valuation query FAILED: ${e.message}")
            0.0
        }
    }

    suspend fun getLowStockAlerts(threshold: Double = 5.0): List<Service> {
        val startTime = getCurrentTimestamp()
        KMPLogger.log("[PERF] Low stock alerts query START")
        
        val snapshot = getServicesRef().where { "stockQuantity" lessThanOrEqualTo threshold }.get()
        val duration = getCurrentTimestamp() - startTime
        val count = snapshot.documents.size
        
        KMPLogger.log("[PERF] Low stock alerts query END: $duration ms. Documents: $count")

        return snapshot.documents.map { it.data(Service.serializer()) }
    }

    suspend fun getExpiringSoon(days: Int = 30): List<InventoryBatchEntity> {
        val threshold = now() + (days * 24 * 60 * 60 * 1000L)
        val snapshot = getBatchesRef()
            .where { "remainingQuantity" greaterThan 0.0 }
            .where { "expiryDate" lessThanOrEqualTo threshold }
            .get()
        return snapshot.documents.map { it.data(InventoryBatchEntity.serializer()) }
    }

    // --- STOCK LEDGER ---
    suspend fun getStockLedger(productId: String, start: Long, end: Long): Map<String, Double> {
        val snapshot = getMovementsRef()
            .where { "productId" equalTo productId }
            .where { "timestamp" greaterThanOrEqualTo start }
            .where { "timestamp" lessThanOrEqualTo end }
            .get()
        
        val movements = snapshot.documents.map { it.data(StockMovementEntity.serializer()) }
        
        val purchases = movements.filter { it.type == "PURCHASE" }.sumOf { it.quantity }
        val sales = movements.filter { it.type == "SALE" }.sumOf { it.quantity }
        val returns = movements.filter { it.type.startsWith("RETURN") }.sumOf { it.quantity }
        val damages = movements.filter { it.type == "DAMAGE" }.sumOf { it.quantity }
        
        return mapOf(
            "Purchases" to purchases,
            "Sales" to -abs(sales), 
            "Returns" to returns,
            "Damages" to -abs(damages)
        )
    }

    // --- PURCHASE ORDERS ---
    suspend fun createPurchaseOrder(po: PurchaseOrderEntity, items: List<PurchaseOrderItemEntity>) {
        val t = now()
        val id = if (po.id.isEmpty() || po.id == "po_0") "po_$t" else po.id
        val finalPO = po.copy(id = id, timestamp = t)
        println("DEBUG: [LOGIN] Attempting Firestore path: purchase_orders/$id")
        getPoRef().document(id).set(PurchaseOrderEntity.serializer(), finalPO)
        
        val itemsRef = FirestoreManager.getCompanyRef().collection("purchase_order_items")
        items.forEach { item ->
            val itemId = "poi_${now()}"
            println("DEBUG: [LOGIN] Attempting Firestore path: purchase_order_items/$itemId")
            itemsRef.document(itemId).set(PurchaseOrderItemEntity.serializer(), item.copy(id = itemId, poId = id))
        }
    }

    fun getAllPurchaseOrders(): Flow<List<PurchaseOrderEntity>> = getPoRef().snapshots().map { snapshot ->
        snapshot.documents.map { it.data(PurchaseOrderEntity.serializer()) }
    }

    suspend fun updatePOStatus(poId: String, status: String) {
        if (poId.isEmpty()) return
        println("DEBUG: [LOGIN] Attempting Firestore path: purchase_orders/$poId")
        getPoRef().document(poId).update("status" to status)
    }
}
