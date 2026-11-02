package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class Company(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val businessRegistrationNumber: String = "",
    val kraPin: String = "",
    val eTimsConfig: Map<String, String> = emptyMap(),
    val mpesaConfig: Map<String, String> = emptyMap(),
    val businessType: String = "",
    val country: String = "Kenya",
    val county: String = "",
    val town: String = "",
    val physicalAddress: String = "",
    val phone: String = "",
    val email: String = "",
    val logo: String = "",
    val theme: CompanyTheme = CompanyTheme(),
    val subscriptionPlan: String = "Basic",
    val subscriptionStatus: String = "Trial", // Active, Inactive, Suspended, Trial, Expired
    val adminName: String = "",
    val adminEmail: String = "",
    val adminPhone: String = "",
    val activationCode: String = "",
    val printerDetails: String = "",
    val signatureUrl: String = "",
    val stampUrl: String = "",
    val authorizedPerson: String = "",
    val managerName: String = "",
    val active: Boolean = true,
    val companyMotto: String = "",
    val website: String = "",
    val timezone: String = "Africa/Nairobi",
    val invoicePrefix: String = "INV-",
    val receiptPrefix: String = "REC-",
    val quotationPrefix: String = "QTN-",
    val purchasePrefix: String = "PUR-",
    val createdDate: Long = 0,
    val createdBy: String = "",
    val lastLogin: Long = 0,
    val staffCount: Int = 0,
    val deviceCount: Int = 0,
    val productCount: Int = 0
)

@Serializable
data class CompanyTheme(
    val primaryColor: String = "#0D47A1",
    val accentColor: String = "#1976D2",
    val receiptHeader: String = "",
    val receiptFooter: String = "",
    val currency: String = "KES",
    val timezone: String = "Africa/Nairobi"
)
