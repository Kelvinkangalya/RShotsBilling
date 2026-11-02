package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.Service
import com.example.rshotsbilling.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ServiceViewModel : ViewModel() {

    private val repository by lazy { ServiceRepository() }

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    fun loadServices() {
        viewModelScope.launch {
            val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
            com.example.rshotsbilling.utils.KMPLogger.log("[PERF] ServiceViewModel loadServices START")
            repository.getServicesFlow().collect { list ->
                val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
                com.example.rshotsbilling.utils.KMPLogger.log("[PERF] ServiceViewModel loadServices END (First list): $duration ms. Count: ${list.size}")
                _services.value = list
            }
        }
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            try {
                repository.addService(service)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteService(id)
                loadServices()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
