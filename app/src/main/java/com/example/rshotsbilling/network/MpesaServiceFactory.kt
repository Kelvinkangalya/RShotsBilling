package com.example.rshotsbilling.network

import android.content.Context

object MpesaServiceFactory {
    fun create(context: Context): AndroidMpesaService {
        val prefs = context.getSharedPreferences("mpesa_settings", Context.MODE_PRIVATE)
        val isMockMode = prefs.getBoolean("is_mock_mode", true)
        
        return if (isMockMode) {
            AndroidMockMpesaService()
        } else {
            AndroidDarajaMpesaService(
                shortcode = prefs.getString("shortcode", "") ?: "",
                consumerKey = prefs.getString("consumer_key", "") ?: "",
                consumerSecret = prefs.getString("consumer_secret", "") ?: "",
                passkey = prefs.getString("passkey", "") ?: "",
                environment = prefs.getString("environment", "Sandbox") ?: "Sandbox"
            )
        }
    }
}
