package com.example.rshotsbilling.utils

import java.io.File
import java.io.FileOutputStream
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

actual object ReportPlatform {
    actual fun getLogoBytes(): ByteArray? {
        return try {
            val stream = object {}.javaClass.getResourceAsStream("/logo.png")
            stream?.readBytes()
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun getRemoteLogoBytes(url: String): ByteArray? {
        if (url.isEmpty()) return null
        val client = HttpClient()
        return try {
            val response = client.get(url)
            response.readBytes()
        } catch (e: Exception) {
            null
        } finally {
            client.close()
        }
    }

    actual fun saveReport(bytes: ByteArray, fileName: String, subdirectory: String): String {
        val userHome = System.getProperty("user.home")
        val dir = File(userHome, "RShotsBilling/$subdirectory")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        return file.absolutePath
    }
}
