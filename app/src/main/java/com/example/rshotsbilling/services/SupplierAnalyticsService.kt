package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.PurchaseOrderEntity

/**
 * Business intelligence for supplier performance.
 */
data class AndroidSupplierSummary(
    val supplierId: String,
    val totalOrders: Int,
    val totalSpent: Double,
    val lastOrderDate: Long?,
    val pendingOrders: Int
)

object AndroidSupplierAnalyticsService {

    /**
     * Aggregates purchase orders into a supplier summary.
     */
    fun generateSupplierSummary(
        supplierId: String,
        allOrders: List<PurchaseOrderEntity>
    ): SupplierSummary {
        val supplierOrders = allOrders.filter { it.supplierId == supplierId }
        
        return SupplierSummary(
            supplierId = supplierId,
            totalOrders = supplierOrders.size,
            totalSpent = supplierOrders.sumOf { it.totalAmount },
            lastOrderDate = supplierOrders.maxOfOrNull { it.timestamp },
            pendingOrders = supplierOrders.count { it.status == "PENDING" }
        )
    }
}
