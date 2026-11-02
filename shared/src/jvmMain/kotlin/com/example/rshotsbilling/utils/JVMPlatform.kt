package com.example.rshotsbilling.utils

import com.google.firebase.FirebasePlatform
import java.util.prefs.Preferences

/**
 * JVM Implementation of the Firebase Platform emulation layer.
 */
class JVMFirebasePlatform : FirebasePlatform() {
    private val prefs = Preferences.userRoot().node("com.example.rshotsbilling")

    override fun store(key: String, value: String) {
        prefs.put(key, value)
    }

    override fun retrieve(key: String): String? {
        return prefs.get(key, null)
    }

    override fun clear(key: String) {
        prefs.remove(key)
    }

    override fun log(msg: String) {
        DesktopLogger.log("[Firebase-Native] $msg")
    }

    companion object {
        fun initialize() {
            try {
                val platformInstance = JVMFirebasePlatform()
                
                // Use the official public method if available, fallback to reflection
                try {
                    FirebasePlatform.initializeFirebasePlatform(platformInstance)
                    DesktopLogger.log("DEBUG: [Firebase-JVM] FirebasePlatform initialized via public method.")
                } catch (e: NoSuchMethodError) {
                    DesktopLogger.log("DEBUG: [Firebase-JVM] public initializeFirebasePlatform not found, using reflection...")
                    val companion = FirebasePlatform.Companion
                    val methods = companion.javaClass.declaredMethods
                    val setter = methods.find { 
                        it.name.startsWith("setFirebasePlatform") || 
                        (it.parameterCount == 1 && it.parameterTypes[0] == FirebasePlatform::class.java) 
                    }
                    
                    if (setter != null) {
                        setter.isAccessible = true
                        setter.invoke(companion, platformInstance)
                        DesktopLogger.log("DEBUG: [Firebase-JVM] FirebasePlatform set successfully via reflection.")
                    } else {
                        DesktopLogger.log("ERROR: [Firebase-JVM] Could not find setFirebasePlatform method.")
                    }
                }
            } catch (e: Exception) {
                DesktopLogger.log("ERROR: [Firebase-JVM] Failed to set FirebasePlatform: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
