package com.example.rshotsbilling.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

actual object ReportPlatform {
    actual fun getLogoBytes(): ByteArray? {
        return try {
            val context = SharedContext.appContext
            val resId = context.resources.getIdentifier("logo", "drawable", context.packageName)
            if (resId == 0) return null
            
            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
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
        val context = SharedContext.appContext
        val dir = File(context.getExternalFilesDir(null), subdirectory)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        return file.absolutePath
    }
}
