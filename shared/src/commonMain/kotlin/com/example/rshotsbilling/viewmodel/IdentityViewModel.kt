package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.InstallationProfile
import com.example.rshotsbilling.repository.IdentityRepository
import com.example.rshotsbilling.utils.getDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class IdentityUiState(
    val profile: InstallationProfile = InstallationProfile(),
    val isSyncing: Boolean = false,
    val lastSyncStatus: String = "Unknown",
    val isOnline: Boolean = true // Simplified for now
)

class IdentityViewModel : ViewModel() {
    private val repository = IdentityRepository()
    
    private val _uiState = MutableStateFlow(IdentityUiState(profile = getDeviceInfo()))
    val uiState: StateFlow<IdentityUiState> = _uiState

    init {
        println("DEBUG: [ViewModel] IdentityViewModel Initialized. Installation ID: ${_uiState.value.profile.installationId}")
    }

    fun syncDevice(firebaseUid: String? = null) {
        viewModelScope.launch {
            println("DEBUG: [Sync] syncDevice started for $firebaseUid")
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            try {
                val currentProfile = _uiState.value.profile.copy(
                    lastLogin = Clock.System.now().toEpochMilliseconds(),
                    firebaseUid = firebaseUid ?: _uiState.value.profile.firebaseUid
                )
                
                println("DEBUG: [Sync] Calling repository.registerOrUpdateDevice...")
                val success = repository.registerOrUpdateDevice(currentProfile)
                println("DEBUG: [Sync] repository.registerOrUpdateDevice returned $success")
                
                _uiState.value = _uiState.value.copy(
                    profile = currentProfile,
                    lastSyncStatus = if (success) "Success" else "Failure"
                )
            } catch (e: Exception) {
                println("DEBUG: [Sync] IdentityViewModel syncDevice FAILED: ${e.message}")
                _uiState.value = _uiState.value.copy(lastSyncStatus = "Error")
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }
}
