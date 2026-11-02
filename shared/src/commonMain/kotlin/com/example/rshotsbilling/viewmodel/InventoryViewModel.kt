package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.*
import com.example.rshotsbilling.repository.InventoryRepository
import com.example.rshotsbilling.repository.ServiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    private val inventoryRepository by lazy { InventoryRepository() }
    private val serviceRepository by lazy { ServiceRepository() }

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    private val _suppliers = MutableStateFlow<List<SupplierEntity>>(emptyList())
    val suppliers: StateFlow<List<SupplierEntity>> = _suppliers.asStateFlow()

    private val _purchaseOrders = MutableStateFlow<List<PurchaseOrderEntity>>(emptyList())
    val purchaseOrders: StateFlow<List<PurchaseOrderEntity>> = _purchaseOrders.asStateFlow()

    private val _expiringSoonProducts = MutableStateFlow<List<InventoryBatchEntity>>(emptyList())
    val expiringSoonProducts: StateFlow<List<InventoryBatchEntity>> = _expiringSoonProducts.asStateFlow()

    private val _stockValue = MutableStateFlow(0.0)
    val stockValue: StateFlow<Double> = _stockValue.asStateFlow()

    fun loadData() {
        loadServices()
        loadSuppliers()
        loadPurchaseOrders()
        loadExpiringSoon()
        loadStockValue()
    }

    private fun loadStockValue() {
        viewModelScope.launch {
            _stockValue.value = inventoryRepository.calculateStockValue()
        }
    }

    private fun loadServices() {
        viewModelScope.launch {
            _services.value = serviceRepository.getServices()
        }
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            inventoryRepository.getAllSuppliers().collect { list ->
                _suppliers.value = list
            }
        }
    }

    private fun loadPurchaseOrders() {
        viewModelScope.launch {
            inventoryRepository.getAllPurchaseOrders().collect { list ->
                _purchaseOrders.value = list
            }
        }
    }

    private fun loadExpiringSoon() {
        viewModelScope.launch {
            inventoryRepository.getExpiringSoonBatches(0).collect { list ->
                _expiringSoonProducts.value = list
            }
        }
    }

    fun getMovementsForProduct(productId: String) = inventoryRepository.getMovementsForProduct(productId)
    fun getBatchesForProduct(productId: String) = inventoryRepository.getBatchesForProduct(productId)

    val totalStockValue = _stockValue.asStateFlow()

    val lowStockCount = _services.map { list ->
        list.count { it.stockQuantity >= 0.0 && it.stockQuantity <= 5.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockCount = _services.map { list ->
        list.count { it.stockQuantity == 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            inventoryRepository.addSupplier(supplier)
        }
    }

    fun createPurchaseOrder(po: PurchaseOrderEntity, items: List<PurchaseOrderItemEntity>) {
        viewModelScope.launch {
            inventoryRepository.createPurchaseOrder(po, items)
        }
    }

    fun updatePOStatus(poId: String, status: String) {
        viewModelScope.launch {
            inventoryRepository.updatePOStatus(poId, status)
        }
    }

    fun adjustStock(productId: String, quantity: Double, type: String, reason: String) {
        viewModelScope.launch {
            inventoryRepository.logMovement(
                productId = productId,
                quantity = quantity,
                type = type,
                reason = reason,
                referenceId = "ADJ-${now()}"
            )
        }
    }
    
    private fun now() = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}
