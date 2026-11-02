package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

actual fun initializeFirebase() {
    if (FirebaseStatus.state.value == FirebaseState.Ready) return
    
    FirebaseStatus.update(FirebaseState.Initializing)
    DesktopLogger.log("[Firebase-JVM] Initialization started")
    
    try {
        // 1. Initialize the Platform emulation layer
        JVMFirebasePlatform.initialize()
        DesktopLogger.log("[Firebase-JVM] Configuration loaded")
        
        // 2. Initialize Firebase with shared options
        try {
            DesktopLogger.log("[Firebase-JVM] Attempting Firebase.initialize with options...")
            
            // CRITICAL FIX: The GitLive SDK for JVM has a known issue with the android.app.Application cast 
            // in some packaged environments. We try to use the most basic initialization possible.
            
            try {
                // Try official method first
                Firebase.initialize(options = FirebaseConfig.options)
                DesktopLogger.log("[Firebase-JVM] Firebase application initialized with default options.")
            } catch (e1: Exception) {
                DesktopLogger.log("WARNING: [Firebase-JVM] Simple init failed: ${e1.message}. Trying with dummy context...")
                // Fallback to dummy context if simple init fails
                val dummyApplication = try {
                    android.app.Application()
                } catch (t: Throwable) {
                    DesktopLogger.log("WARNING: [Firebase-JVM] Could not instantiate android.app.Application: ${t.message}")
                    null
                }
                
                if (dummyApplication != null) {
                    Firebase.initialize(context = dummyApplication, options = FirebaseConfig.options)
                    DesktopLogger.log("[Firebase-JVM] Firebase application initialized with dummy Application.")
                } else {
                    DesktopLogger.log("CRITICAL: [Firebase-JVM] No initialization strategy succeeded.")
                    throw e1
                }
            }
        } catch (e: Exception) {
            DesktopLogger.log("CRITICAL: [Firebase-JVM] All initialization attempts failed: ${e.message}")
            throw e
        }
        
        // 3. Force provider initialization and log
        try {
            val auth = Firebase.auth
            DesktopLogger.log("[Firebase-JVM] Auth provider initialized")
            
            val db = Firebase.firestore
            DesktopLogger.log("[Firebase-JVM] Firestore provider initialized")
        } catch (pe: Exception) {
            DesktopLogger.log("WARNING: [Firebase-JVM] Post-init provider check failed: ${pe.message}")
        }
        
        DesktopLogger.log("[Firebase-JVM] Initialization READY")
        FirebaseStatus.update(FirebaseState.Ready)
        
    } catch (e: Exception) {
        val sw = java.io.StringWriter()
        e.printStackTrace(java.io.PrintWriter(sw))
        val errorMsg = "CRITICAL: [Firebase-JVM] Initialization FAILED\n" +
                      "Exception: ${e.javaClass.name}\n" +
                      "Message: ${e.message}\n" +
                      "Stack: ${sw.toString()}\n" +
                      "Project ID: ${FirebaseConfig.options.projectId}\n" +
                      "Platform: JVM/Desktop"
        DesktopLogger.log(errorMsg)
        FirebaseStatus.update(FirebaseState.Error)
    }
}
