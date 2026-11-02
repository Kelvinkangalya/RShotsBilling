package com.example.rshotsbilling

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.rshotsbilling.utils.initializeFirebase
import javax.swing.JOptionPane

fun main() {
    try {
        println("DEBUG: [Main] Starting application...")
        
        // Initialize Firebase at the platform entry point
        initializeFirebase()
        
        application {
            Window(
                onCloseRequest = ::exitApplication, 
                title = "Rshots Billing"
            ) {
                App()
            }
        }
    } catch (t: Throwable) {
        val errorMsg = "Application failed to start:\n${t.message}\n\nCheck logs for details."
        System.err.println("DEBUG: [Main] CRITICAL ERROR: ${t.message}")
        t.printStackTrace()
        
        try {
            JOptionPane.showMessageDialog(null, errorMsg, "Rshots Billing - Error", JOptionPane.ERROR_MESSAGE)
        } catch (e: Exception) {
            System.err.println("DEBUG: [Main] Could not show JOptionPane: ${e.message}")
        }
    }
}
