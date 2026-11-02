package com.example.rshotsbilling.utils

object BrandingManager {
    suspend fun getCompanyLogoBytes(): ByteArray? {
        val url = UserSession.companyLogo
        if (url.isNotEmpty()) {
            val remote = ReportPlatform.getRemoteLogoBytes(url)
            if (remote != null) return remote
        }
        return ReportPlatform.getLogoBytes()
    }
}
