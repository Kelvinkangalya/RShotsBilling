package com.example.rshotsbilling.utils

object NormalizationUtils {

    /**
     * Normalizes a phone number by removing all non-digit characters.
     */
    fun normalizePhone(phone: String): String {
        return phone.filter { it.isDigit() }
    }

    /**
     * Normalizes an email by trimming and converting to lowercase.
     */
    fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    /**
     * Normalizes a name by trimming, removing redundant spaces, and converting to title case.
     */
    fun normalizeName(name: String): String {
        return name.trim().split(" ").filter { it.isNotEmpty() }
            .joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
    }
}
