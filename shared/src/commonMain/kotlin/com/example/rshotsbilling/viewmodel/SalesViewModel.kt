package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.Receipt
import com.example.rshotsbilling.repository.SalesRepository
import com.example.rshotsbilling.repository.EtimsRepository
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.KMPLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalesViewModel : ViewModel() {

    private val repository by lazy { SalesRepository() }
    private val etimsRepository by lazy { EtimsRepository() }

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    private val _etimsSales = MutableStateFlow<List<Sale>>(emptyList())
    val etimsSales: StateFlow<List<Sale>> = _etimsSales.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadSales() {
        viewModelScope.launch {
            val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
            KMPLogger.log("[PERF] SalesViewModel loadSales START")
            repository.getSalesFlow().collect { list ->
                val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
                KMPLogger.log("[PERF] SalesViewModel loadSales END (First list): $duration ms. Count: ${list.size}")
                _sales.value = list
                _etimsSales.value = list.filter { it.isEtims }
            }
        }
    }

    fun addSale(
        sale: Sale,
        receiptViewModel: ReceiptViewModel? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                var saleToSave = sale
                
                if (sale.isEtims) {
                    val vatRate = BusinessConfig.VAT_RATE
                    val taxableAmount = sale.total / (1 + vatRate)
                    val vatAmount = sale.total - taxableAmount
                    
                    saleToSave = sale.copy(
                        taxableAmount = taxableAmount,
                        vatAmount = vatAmount,
                        taxAmount = vatAmount,
                        etimsStatus = "PENDING"
                    )
                }

                val savedSale = repository.addSale(saleToSave)
                
                if (savedSale.isEtims) {
                    etimsRepository.submitSaleToEtims(savedSale)
                }

                receiptViewModel?.addReceipt(
                    Receipt(
                        saleId = savedSale.id,
                        invoiceNumber = savedSale.invoiceNumber,
                        customerName = savedSale.customerName,
                        totalAmount = savedSale.total,
                        pdfPath = ""
                    )
                )

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save sale"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resubmitFailedEtims() {
        viewModelScope.launch {
            etimsRepository.resubmitPendingSales()
        }
    }

    fun approveSale(saleId: String, reason: String = "Approved") {
        viewModelScope.launch {
            try {
                repository.updateSaleStatus(saleId, "APPROVED", reason)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectSale(saleId: String, reason: String) {
        viewModelScope.launch {
            try {
                repository.updateSaleStatus(saleId, "REJECTED", reason)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSale(saleId: String, reason: String) {
        viewModelScope.launch {
            try {
                repository.updateSaleStatus(saleId, "DELETED", reason)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
