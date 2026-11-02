package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.CustomerEntity
import com.example.rshotsbilling.repository.CustomerRepository
import com.example.rshotsbilling.repository.SalesRepository
import com.example.rshotsbilling.utils.KMPLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerViewModel : ViewModel() {
    private val repository = CustomerRepository()
    private val salesRepo = SalesRepository()

    private val _customers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    val customers: StateFlow<List<CustomerEntity>> = _customers.asStateFlow()

    private val _isMigrating = MutableStateFlow(false)
    val isMigrating: StateFlow<Boolean> = _isMigrating.asStateFlow()

    fun loadCustomers() {
        viewModelScope.launch {
            repository.getCustomersFlow().collect {
                _customers.value = it
            }
        }
    }

    fun addCustomer(customer: CustomerEntity, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addOrUpdateCustomer(customer)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun searchCustomers(query: String): List<CustomerEntity> {
        if (query.isEmpty()) return _customers.value
        val q = query.lowercase()
        return _customers.value.filter {
            it.name.lowercase().contains(q) || 
            it.phone.contains(q) || 
            it.email.lowercase().contains(q)
        }
    }

    fun triggerMigration() {
        viewModelScope.launch {
            _isMigrating.value = true
            try {
                val sales = salesRepo.getSales()
                repository.migrateFromHistory(sales)
                KMPLogger.log("DEBUG: [CustomerViewModel] Migration triggered successfully.")
            } catch (e: Exception) {
                KMPLogger.log("ERROR: [CustomerViewModel] Migration failed: ${e.message}")
            } finally {
                _isMigrating.value = false
            }
        }
    }
}
