package com.example.rshotsbilling.utils

actual object KMPLogger {
    actual fun log(message: String) {
        DesktopLogger.log(message)
    }
}
