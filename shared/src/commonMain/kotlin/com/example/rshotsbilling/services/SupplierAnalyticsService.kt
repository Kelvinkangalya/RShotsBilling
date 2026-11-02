package com.example.rshotsbilling.services

import com.example.rshotsbilling.model.PurchaseOrderEntity

data class SupplierSummary(
    val supplierId: String,
    val totalOrders: Int,
    val totalSpent: Double,
    val lastOrderDate: Long?,
    val pendingOrders: Int
)

object SupplierAnalyticsService {

    fun generateSupplierSummary(supplierId: String, orders: List<PurchaseOrderEntity>): SupplierSummary {
        val supplierOrders = orders.filter { it.supplierId == supplierId }
        return SupplierSummary(
            supplierId = supplierId,
            totalOrders = supplierOrders.size,
            totalSpent = supplierOrders.sumOf { it.totalAmount },
            lastOrderDate = supplierOrders.maxOfOrNull { it.timestamp },
            pendingOrders = supplierOrders.count { it.status == "PENDING" }
        )
    }
}
