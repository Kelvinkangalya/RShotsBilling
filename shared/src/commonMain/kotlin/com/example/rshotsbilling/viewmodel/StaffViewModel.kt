package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.StaffMember
import com.example.rshotsbilling.model.PasswordResetRequest
import com.example.rshotsbilling.repository.StaffRepository
import com.example.rshotsbilling.utils.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

class StaffViewModel : ViewModel() {
    private val repo = StaffRepository()

    private val _staffList = MutableStateFlow<List<StaffMember>>(emptyList())
    val staffList: StateFlow<List<StaffMember>> = _staffList

    private val _resetRequests = MutableStateFlow<List<PasswordResetRequest>>(emptyList())
    val resetRequests: StateFlow<List<PasswordResetRequest>> = _resetRequests

    init {
        // Removed automatic loading to prevent PERMISSION_DENIED on startup before login
    }

    fun observeResetRequests() {
        viewModelScope.launch {
            repo.getPasswordResetRequestsFlow().collect {
                _resetRequests.value = it
            }
        }
    }

    private fun observeStaff() {
        viewModelScope.launch {
            while (true) {
                try {
                    repo.getStaffListFlow().collectLatest { list ->
                        _staffList.value = list
                    }
                } catch (e: Exception) {
                    delay(5000)
                }
            }
        }
    }

    fun loadStaff() {
        viewModelScope.launch {
            try {
                _staffList.value = repo.getStaffList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addStaff(staff: StaffMember) {
        viewModelScope.launch {
            try {
                repo.addStaff(staff)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteStaff(id: String) {
        viewModelScope.launch {
            try {
                repo.deleteStaff(id)
                loadStaff()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun approveResetRequest(request: PasswordResetRequest) {
        viewModelScope.launch {
            try {
                val updated = request.copy(
                    status = "APPROVED",
                    processedBy = UserSession.userEmail,
                    processedAt = Clock.System.now().toEpochMilliseconds()
                )
                repo.updatePasswordResetRequest(updated)
                // In a real app, this would also trigger dev.gitlive.firebase.auth.auth.sendPasswordResetEmail(request.email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectResetRequest(request: PasswordResetRequest) {
        viewModelScope.launch {
            try {
                val updated = request.copy(
                    status = "REJECTED",
                    processedBy = UserSession.userEmail,
                    processedAt = Clock.System.now().toEpochMilliseconds()
                )
                repo.updatePasswordResetRequest(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun purgeAllStaffExcept(keepEmail: String) {
        viewModelScope.launch {
            try {
                val currentList = repo.getStaffList()
                currentList.forEach { staff ->
                    if (!staff.email.equals(keepEmail, ignoreCase = true)) {
                        repo.deleteStaff(staff.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
