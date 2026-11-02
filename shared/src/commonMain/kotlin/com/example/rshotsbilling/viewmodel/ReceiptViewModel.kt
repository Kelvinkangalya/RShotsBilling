package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.Receipt
import com.example.rshotsbilling.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ReceiptViewModel : ViewModel() {

    private val repository by lazy { ReceiptRepository() }

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadReceipts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getReceiptsFlow().collect { list ->
                    _receipts.value = list.sortedByDescending { it.date }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load receipts"
                _isLoading.value = false
            }
        }
    }

    fun addReceipt(receipt: Receipt) {
        viewModelScope.launch {
            try {
                repository.addReceipt(receipt)
                loadReceipts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save receipt"
            }
        }
    }

    fun searchReceipts(query: String): List<Receipt> {
        return receipts.value.filter {
            it.customerName.contains(query, ignoreCase = true) ||
            it.invoiceNumber.contains(query, ignoreCase = true) ||
            it.customerPhone.contains(query, ignoreCase = true)
        }
    }

    fun refresh() {
        loadReceipts()
    }

    fun clearError() {
        _error.value = null
    }

    fun deleteReceipt(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteReceipt(id)
                loadReceipts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteReceiptsByInvoice(invoiceNumber: String) {
        viewModelScope.launch {
            try {
                repository.deleteReceiptsByInvoice(invoiceNumber)
                loadReceipts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
