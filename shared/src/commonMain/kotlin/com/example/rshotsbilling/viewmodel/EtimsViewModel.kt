package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.EtimsInvoice
import com.example.rshotsbilling.repository.EtimsRepository
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class EtimsViewModel : ViewModel() {
    private val repository by lazy { EtimsRepository() }

    private val _allInvoices = MutableStateFlow<List<EtimsInvoice>>(emptyList())
    val allInvoices: StateFlow<List<EtimsInvoice>> = _allInvoices.asStateFlow()

    fun loadInvoices() {
        viewModelScope.launch {
            repository.getAllInvoicesFlow().collect { list ->
                _allInvoices.value = list
            }
        }
    }

    val totalTaxCollected = allInvoices.map { list ->
        list.sumOf { it.taxAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalInvoices = allInvoices.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingInvoicesCount = allInvoices.map { list ->
        list.count { it.status == "Pending" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val submittedInvoicesCount = allInvoices.map { list ->
        list.count { it.status == "Submitted" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val failedInvoicesCount = allInvoices.map { list ->
        list.count { it.status == "Failed" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayTaxCollected = allInvoices.map { list ->
        val startOfDay = getStartOfDay()
        list.filter { it.timestamp >= startOfDay }.sumOf { it.taxAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todaySubmissionsCount = allInvoices.map { list ->
        val startOfDay = getStartOfDay()
        list.count { it.timestamp >= startOfDay && it.status == "Submitted" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun getStartOfDay(): Long {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val startOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 0, 0, 0, 0)
        return startOfDay.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    fun retrySubmission(invoice: EtimsInvoice) {
        viewModelScope.launch {
            repository.updateInvoice(invoice.copy(status = "Pending"))
        }
    }

    fun deleteInvoice(invoice: EtimsInvoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice.id)
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
