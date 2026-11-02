package com.example.rshotsbilling.network

import android.content.Context

object EtimsServiceFactory {
    fun create(context: Context): AndroidEtimsService {
        val prefs = context.getSharedPreferences("etims_settings", Context.MODE_PRIVATE)
        val isMockMode = prefs.getBoolean("is_mock_mode", true)
        val apiUrl = prefs.getString("api_url", "https://api.kra.go.ke/") ?: ""

        return if (isMockMode) {
            AndroidMockEtimsService()
        } else {
            AndroidKRAEtimsService(apiUrl)
        }
    }
}
