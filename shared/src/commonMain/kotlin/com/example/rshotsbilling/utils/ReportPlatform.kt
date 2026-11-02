package com.example.rshotsbilling.utils

/**
 * Interface to provide platform-specific resources and file handling
 * for report generation in the shared module.
 */
expect object ReportPlatform {
    /**
     * Loads the company logo as a ByteArray.
     */
    fun getLogoBytes(): ByteArray?

    /**
     * Loads a remote logo as a ByteArray.
     */
    suspend fun getRemoteLogoBytes(url: String): ByteArray?

    /**
     * Saves a report file to the platform's specific storage.
     * @param bytes The content of the report.
     * @param fileName The name of the file to create.
     * @param subdirectory The subdirectory within the platform's report location.
     * @return The absolute path to the saved file.
     */
    fun saveReport(bytes: ByteArray, fileName: String, subdirectory: String): String
}
