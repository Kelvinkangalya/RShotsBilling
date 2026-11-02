package com.example.rshotsbilling.utils

/**
 * Shared context provider for JVM (Desktop).
 * Provides a dummy context for libraries that expect an Android-like environment.
 */
object SharedContext {
    val appContext: Any by lazy {
        try {
            val contextClass = Class.forName("android.content.Context")
            val constructor = contextClass.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        } catch (e: Exception) {
            println("WARNING: [SharedContext] Failed to create dummy Context: ${e.message}")
            Unit // Fallback to Unit if all else fails
        }
    }
}
