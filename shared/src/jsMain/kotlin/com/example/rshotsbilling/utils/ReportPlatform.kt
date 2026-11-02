package com.example.rshotsbilling.utils

actual object ReportPlatform {
    actual fun getLogoBytes(): ByteArray? = null
    actual fun saveReport(bytes: ByteArray, fileName: String, subdirectory: String): String = ""
}
