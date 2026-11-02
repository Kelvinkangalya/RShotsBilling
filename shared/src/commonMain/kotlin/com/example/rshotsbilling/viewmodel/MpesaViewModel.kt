package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.MpesaTransaction
import com.example.rshotsbilling.repository.MpesaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class MpesaViewModel : ViewModel() {
    private val repository by lazy { MpesaRepository() }
    
    private val _allTransactions = MutableStateFlow<List<MpesaTransaction>>(emptyList())
    val allTransactions: StateFlow<List<MpesaTransaction>> = _allTransactions.asStateFlow()

    fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactionsFlow().collect { list ->
                _allTransactions.value = list
            }
        }
    }

    val totalRevenue = allTransactions.map { list ->
        list.filter { it.status == "SUCCESS" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayRevenue = allTransactions.map { list ->
        val startOfDay = getStartOfDay()
        list.filter { it.status == "SUCCESS" && it.createdAt >= startOfDay }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val successCount = allTransactions.map { list ->
        list.count { it.status == "SUCCESS" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingCount = allTransactions.map { list ->
        list.count { it.status == "PENDING" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val failedCount = allTransactions.map { list ->
        list.count { it.status == "FAILED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun getStartOfDay(): Long {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val startOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 0, 0, 0, 0)
        return startOfDay.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    fun initiatePayment(phone: String, amount: Double, saleId: String) {
        viewModelScope.launch {
            repository.initiateStkPush(saleId, phone, amount)
        }
    }

    fun retryTransaction(transaction: MpesaTransaction) {
        viewModelScope.launch {
            transaction.transactionId?.let { repository.checkStatus(it) }
        }
    }

    fun syncNow() {
    }

    fun getSettings(): Map<String, Any> {
        return emptyMap()
    }

    fun saveSettings(vararg args: Any) {
    }
}
