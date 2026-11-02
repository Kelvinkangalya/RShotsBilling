package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.InstallationProfile

/**
 * Utility to get a unique identifier for the current device.
 * Used for activation and security locking.
 */
expect fun getDeviceSerialNumber(): String

/**
 * Get the permanent Installation ID for this specific installation.
 */
expect fun getInstallationId(): String

/**
 * Collect all device profile information.
 */
expect fun getDeviceInfo(): InstallationProfile
