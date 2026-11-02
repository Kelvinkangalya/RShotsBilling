package com.example.rshotsbilling.utils

import android.util.Log

actual object KMPLogger {
    actual fun log(message: String) {
        Log.d("RShotsBilling", message)
    }
}
