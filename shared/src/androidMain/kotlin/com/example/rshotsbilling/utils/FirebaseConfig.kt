package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

actual fun initializeFirebase() {
    if (FirebaseStatus.state.value == FirebaseState.Ready) return
    
    FirebaseStatus.update(FirebaseState.Initializing)
    KMPLogger.log("DEBUG: [Firebase] Android Initialization starting...")
    try {
        // Initialize Firebase manually on Android too using shared options
        // This reduces dependency on google-services plugin's auto-init
        dev.gitlive.firebase.Firebase.initialize(
            context = SharedContext.appContext,
            options = FirebaseConfig.options
        )
        
        KMPLogger.log("DEBUG: [Firebase] Android Initialization successful.")
        FirebaseStatus.update(FirebaseState.Ready)
    } catch (e: Exception) {
        KMPLogger.log("DEBUG: [Firebase] Android Initialization FAILED: ${e.message}")
        e.printStackTrace()
        FirebaseStatus.update(FirebaseState.Error)
    }
}

// Removed redundant manual options building and FirebaseApp.initializeApp
// because google-services.json handles it automatically in the app module.
