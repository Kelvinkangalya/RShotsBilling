package com.example.rshotsbilling.utils

/**
 * Simple encryption utility for sensitive tokens.
 * In a real-world scenario, use a robust library like SQLCipher or specialized KMP security libs.
 * For this implementation, we use a basic XOR-based obfuscation as a placeholder for sensitive but not critical data.
 */
object EncryptionUtils {
    private const val KEY = "RSHOTS_SECURITY_KEY_2024"

    fun encrypt(data: String): String {
        val encrypted = StringBuilder()
        for (i in data.indices) {
            encrypted.append((data[i].code xor KEY[i % KEY.length].code).toChar())
        }
        return b64Encode(encrypted.toString())
    }

    fun decrypt(encryptedData: String): String {
        val decoded = b64Decode(encryptedData)
        val decrypted = StringBuilder()
        for (i in decoded.indices) {
            decrypted.append((decoded[i].code xor KEY[i % KEY.length].code).toChar())
        }
        return decrypted.toString()
    }

    // Basic Base64 implementations for KMP common module
    private fun b64Encode(input: String): String {
        // Placeholder for actual Base64 encoding which might vary by platform
        // In KMP, you'd typically use a library or expect/actual
        return input // Simplified for this context
    }

    private fun b64Decode(input: String): String {
        return input // Simplified for this context
    }
}
