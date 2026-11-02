package com.example.rshotsbilling.utils

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DesktopLogger {
    private val logDir = File(System.getProperty("user.home"), ".rshotsbilling/logs").apply {
        if (!exists()) mkdirs()
    }
    private val logFile = File(logDir, "desktop.log")
    
    fun log(message: String) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
            val logMessage = "[$timestamp] $message"
            // Use standard error to ensure it's not buffered by gradle in the same way
            System.err.println(logMessage) 
            
            FileWriter(logFile, true).use { fw ->
                PrintWriter(fw).println(logMessage)
            }
        } catch (e: Exception) {
            // Can't do much if logging fails
        }
    }
}
