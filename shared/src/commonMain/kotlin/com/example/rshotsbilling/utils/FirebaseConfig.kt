package com.example.rshotsbilling.utils

import dev.gitlive.firebase.FirebaseOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FirebaseState { Uninitialized, Initializing, Ready, Error }

object FirebaseStatus {
    private val _state = MutableStateFlow(FirebaseState.Uninitialized)
    val state = _state.asStateFlow()
    
    fun update(newState: FirebaseState) {
        println("DEBUG: [FirebaseStatus] State changed to $newState")
        _state.value = newState
    }
}

expect fun initializeFirebase()

object FirebaseConfig {
    val options = FirebaseOptions(
        apiKey = "AIzaSyCGSMbzZ1xQKPQtIpp_Ma0fWUzcd-Q3pMA",
        authDomain = "rshots-billing-system.firebaseapp.com",
        projectId = "rshots-billing-system",
        storageBucket = "rshots-billing-system.firebasestorage.app",
        gcmSenderId = "975661745545",
        applicationId = "1:975661745545:web:38b368b025f357d7788d58",
        gaTrackingId = "G-CQ59E1G7ZV"
    )
}
