package com.example.rshotsbilling

import android.app.Application

import com.example.rshotsbilling.utils.SharedContext
import com.example.rshotsbilling.utils.initializeFirebase

class MainApplication : Application() {
    companion object {
        lateinit var instance: MainApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        println("DEBUG: [MainApplication] onCreate() executed.")
        instance = this
        SharedContext.appContext = this
        initializeFirebase()
    }
}
