package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.repository.PurchaseRepository
import com.example.rshotsbilling.utils.FirestoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PurchaseViewModel : ViewModel() {

    private val repository by lazy { PurchaseRepository() }

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPurchases() {
        viewModelScope.launch {
            val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
            com.example.rshotsbilling.utils.KMPLogger.log("[PERF] PurchaseViewModel loadPurchases START")
            repository.getPurchasesFlow().collect { list ->
                val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
                com.example.rshotsbilling.utils.KMPLogger.log("[PERF] PurchaseViewModel loadPurchases END (First list): $duration ms. Count: ${list.size}")
                _purchases.value = list
            }
        }
    }

    fun addPurchase(purchase: Purchase) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addPurchase(purchase)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add purchase"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approvePurchase(id: String, reason: String = "Approved") {
        viewModelScope.launch {
            try {
                repository.updatePurchaseStatus(id, "APPROVED", reason)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectPurchase(id: String, reason: String) {
        viewModelScope.launch {
            try {
                repository.updatePurchaseStatus(id, "REJECTED", reason)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePurchase(id: String, reason: String = "Deleted") {
        viewModelScope.launch {
            try {
                repository.updatePurchaseStatus(id, "DELETED", reason)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resubmitPurchase(id: String, updatedTitle: String, updatedAmount: Double, note: String) {
        viewModelScope.launch {
            try {
                val purchasesList = purchases.value
                val target = purchasesList.find { it.id == id }
                if (target != null) {
                    val updatedPurchase = target.copy(
                        title = updatedTitle,
                        amount = updatedAmount,
                        totalAmount = updatedAmount,
                        status = "PENDING",
                        auditReason = note
                    )
                    FirestoreManager.getPurchasesRef()
                        .document(id).set(Purchase.serializer(), updatedPurchase)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
