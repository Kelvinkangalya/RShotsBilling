package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.FinancialSummary
import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.repository.PurchaseRepository
import com.example.rshotsbilling.repository.SalesRepository
import kotlinx.datetime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TopProduct(val name: String, val quantity: Double, val revenue: Double) 
data class StaffPerformance(val email: String, val totalSales: Double, val saleCount: Int)
data class StockAlert(val itemName: String, val currentQty: Double) 
data class InventoryMovement(val name: String, val sold: Double, val remaining: Double) 

class ReportsViewModel : ViewModel() {

    private val salesRepo by lazy { SalesRepository() }
    private val purchaseRepo by lazy { PurchaseRepository() }
    private val servicesRepo by lazy { com.example.rshotsbilling.repository.ServiceRepository() }

    private val _summary = MutableStateFlow(FinancialSummary())
    val summary: StateFlow<FinancialSummary> = _summary

    private val _topProducts = MutableStateFlow<List<TopProduct>>(emptyList())
    val topProducts: StateFlow<List<TopProduct>> = _topProducts

    private val _profitTrend = MutableStateFlow<Map<String, Double>>(emptyMap())
    val profitTrend: StateFlow<Map<String, Double>> = _profitTrend

    private val _filteredSales = MutableStateFlow<List<Sale>>(emptyList())
    val filteredSales: StateFlow<List<Sale>> = _filteredSales

    private val _filteredPurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val filteredPurchases: StateFlow<List<Purchase>> = _filteredPurchases

    private val _staffPerformance = MutableStateFlow<List<StaffPerformance>>(emptyList())
    val staffPerformance: StateFlow<List<StaffPerformance>> = _staffPerformance

    private val _lowStockAlerts = MutableStateFlow<List<StockAlert>>(emptyList())
    val lowStockAlerts: StateFlow<List<StockAlert>> = _lowStockAlerts

    private val _inventoryMovement = MutableStateFlow<List<InventoryMovement>>(emptyList())
    val inventoryMovement: StateFlow<List<InventoryMovement>> = _inventoryMovement

    private val _hasPendingApprovals = MutableStateFlow(false)
    val hasPendingApprovals: StateFlow<Boolean> = _hasPendingApprovals

    fun loadReportData(fromMillis: Long, toMillis: Long) {
        viewModelScope.launch {
            try {
                val allSales = salesRepo.getSales()
                val allPurchases = purchaseRepo.getPurchases()

                val pendingExist = allSales.any { it.status == "PENDING" && it.timestamp in fromMillis..toMillis } ||
                                  allPurchases.any { it.status == "PENDING" && (it.date in fromMillis..toMillis || it.timestamp in fromMillis..toMillis) }
                _hasPendingApprovals.value = pendingExist
                
                val activeSales = allSales.filter { it.status != "REJECTED" && it.timestamp in fromMillis..toMillis }
                val activePurchases = allPurchases.filter { it.status != "REJECTED" && (it.date in fromMillis..toMillis || it.timestamp in fromMillis..toMillis) }

                val totalSales = activeSales.sumOf { it.total }
                val totalExpenses = activePurchases.sumOf { it.totalAmount }
                val profit = totalSales - totalExpenses

                val etimsSalesTotal = activeSales.filter { it.isEtims }.sumOf { it.total }
                val nonEtimsSalesTotal = activeSales.filter { !it.isEtims }.sumOf { it.total }
                val totalTax = activeSales.sumOf { it.taxAmount }
                val pendingSubmissions = allSales.count { it.isEtims && (it.etimsStatus == "PENDING" || it.etimsStatus == "FAILED") }
                val failedSubmissionsCount = allSales.count { it.isEtims && it.etimsStatus == "FAILED" }

                _summary.value = FinancialSummary(
                    totalSales = totalSales,
                    totalExpenses = totalExpenses,
                    profit = profit,
                    etimsSales = etimsSalesTotal,
                    nonEtimsSales = nonEtimsSalesTotal,
                    totalTaxCollected = totalTax,
                    vatCollected = totalTax,
                    pendingSubmissions = pendingSubmissions,
                    failedSubmissions = failedSubmissionsCount
                )

                val rangeSales = allSales.filter { it.timestamp in fromMillis..toMillis }
                val rangePurchases = allPurchases.filter { it.date in fromMillis..toMillis || it.timestamp in fromMillis..toMillis }
                _filteredSales.value = rangeSales
                _filteredPurchases.value = rangePurchases

                // Profit Trend Calculation
                val trendData = mutableMapOf<String, Double>()
                rangeSales.groupBy { 
                    val dt = Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
                    "${dt.dayOfMonth}/${dt.monthNumber}"
                }.forEach { (date, sales) ->
                    trendData[date] = sales.sumOf { it.total }
                }
                _profitTrend.value = trendData

                // Best Sellers
                val productMap = mutableMapOf<String, Pair<Double, Double>>()
                activeSales.forEach { sale ->
                    sale.items.forEach { item ->
                        val current = productMap[item.serviceName] ?: Pair(0.0, 0.0)
                        productMap[item.serviceName] = Pair(current.first + item.quantity, current.second + (item.price * item.quantity))
                    }
                }
                _topProducts.value = productMap.map { TopProduct(it.key, it.value.first, it.value.second) }.sortedByDescending { it.quantity }.take(5)

                // Staff Performance
                val perfMap = activeSales.groupBy { it.servedBy }
                    .map { (email, sales) ->
                        StaffPerformance(email = email, totalSales = sales.sumOf { it.total }, saleCount = sales.size)
                    }.sortedByDescending { it.totalSales }
                _staffPerformance.value = perfMap

                // Low Stock
                val services = servicesRepo.getServices()
                _lowStockAlerts.value = services.filter { it.stockQuantity >= 0.0 && it.stockQuantity <= BusinessConfig.LOW_STOCK_THRESHOLD }
                    .map { StockAlert(it.name, it.stockQuantity) }

                // Inventory Velocity
                _inventoryMovement.value = services.map { service ->
                    val soldInPeriod = rangeSales.flatMap { it.items }.filter { it.serviceId == service.id }.sumOf { it.quantity }
                    InventoryMovement(service.name, soldInPeriod, service.stockQuantity)
                }.sortedByDescending { it.sold }

            } catch (e: Exception) {
            }
        }
    }

    fun wipeDataInRange(from: Long, to: Long) {
        viewModelScope.launch {
            salesRepo.clearSalesInRange(from, to)
            purchaseRepo.clearPurchasesInRange(from, to)
            loadReportData(from, to)
        }
    }
}
