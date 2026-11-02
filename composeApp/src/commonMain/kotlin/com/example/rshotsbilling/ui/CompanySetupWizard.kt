package com.example.rshotsbilling.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.Company
import com.example.rshotsbilling.model.CompanyTheme
import com.example.rshotsbilling.repository.CompanyRepository
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.KMPLogger
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import dev.gitlive.firebase.firestore.where

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySetupWizard(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4
    val scope = rememberCoroutineScope()
    val repository = remember { CompanyRepository() }
    
    var company by remember { mutableStateOf<Company?>(null) }
    
    LaunchedEffect(UserSession.companyId) {
        val id = UserSession.companyId
        KMPLogger.log("DEBUG: [Wizard] Loading company for ID: '$id'")
        if (id.isEmpty()) {
            KMPLogger.log("DEBUG: [Wizard] Empty ID. Initializing blank company.")
            company = Company(id = "comp_${com.example.rshotsbilling.utils.getCurrentTimestamp()}")
            return@LaunchedEffect
        }
        
        val loaded = repository.getCompany(id)
        if (loaded == null) {
            KMPLogger.log("WARNING: [Wizard] Company not found. Initializing blank.")
            company = Company(id = id)
        } else {
            company = loaded
        }
    }

    // Wizard State
    var logoUrl by remember { mutableStateOf("") }
    var primaryColor by remember { mutableStateOf("#0D47A1") }
    var accentColor by remember { mutableStateOf("#1976D2") }
    
    var receiptHeader by remember { mutableStateOf("") }
    var receiptFooter by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("KES") }
    
    var kraPin by remember { mutableStateOf("") }
    var etimsApiUrl by remember { mutableStateOf("") }
    var etimsDeviceCode by remember { mutableStateOf("") }
    
    var mpesaShortCode by remember { mutableStateOf("") }
    var mpesaConsumerKey by remember { mutableStateOf("") }
    
    var printerType by remember { mutableStateOf("Thermal") }
    var signatureUrl by remember { mutableStateOf("") }
    var stampUrl by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    if (company == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Company Setup - Step $currentStep of $totalSteps") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LinearProgressIndicator(
                progress = currentStep.toFloat() / totalSteps,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        Text("Branding & Theme", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("Logo URL") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = primaryColor, onValueChange = { primaryColor = it }, label = { Text("Primary Color (Hex)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = accentColor, onValueChange = { accentColor = it }, label = { Text("Accent Color (Hex)") }, modifier = Modifier.fillMaxWidth())
                        
                        Spacer(Modifier.height(16.dp))
                        Text("Preview", style = MaterialTheme.typography.labelSmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(Modifier.size(60.dp).background(parseHexToColor(primaryColor)))
                            Box(Modifier.size(60.dp).background(parseHexToColor(accentColor)))
                        }
                    }
                    2 -> {
                        Text("Receipts & Localization", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = receiptHeader, onValueChange = { receiptHeader = it }, label = { Text("Receipt Header") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = receiptFooter, onValueChange = { receiptFooter = it }, label = { Text("Receipt Footer") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency Code (e.g. KES)") }, modifier = Modifier.fillMaxWidth())
                    }
                    3 -> {
                        Text("Tax & Payments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = kraPin, onValueChange = { kraPin = it }, label = { Text("KRA PIN") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = etimsDeviceCode, onValueChange = { etimsDeviceCode = it }, label = { Text("eTIMS Device Code") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = mpesaShortCode, onValueChange = { mpesaShortCode = it }, label = { Text("M-Pesa Shortcode") }, modifier = Modifier.fillMaxWidth())
                    }
                    4 -> {
                        Text("Hardware & Assets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = printerType, onValueChange = { printerType = it }, label = { Text("Printer Type") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = signatureUrl, onValueChange = { signatureUrl = it }, label = { Text("Digital Signature URL") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = stampUrl, onValueChange = { stampUrl = it }, label = { Text("Company Stamp URL") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { if (currentStep > 1) currentStep-- },
                    enabled = currentStep > 1 && !isLoading
                ) {
                    Text("BACK")
                }
                
                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            isLoading = true
                            scope.launch {
                                try {
                                    val updatedCompany = company!!.copy(
                                        logo = logoUrl,
                                        theme = CompanyTheme(
                                            primaryColor = primaryColor,
                                            accentColor = accentColor,
                                            receiptHeader = receiptHeader,
                                            receiptFooter = receiptFooter,
                                            currency = currency
                                        ),
                                        kraPin = kraPin,
                                        eTimsConfig = mapOf("deviceCode" to etimsDeviceCode),
                                        mpesaConfig = mapOf("shortCode" to mpesaShortCode),
                                        printerDetails = printerType,
                                        signatureUrl = signatureUrl,
                                        stampUrl = stampUrl
                                    )
                                    repository.updateCompanySetup(updatedCompany)
                                    
                                    // Update Staff record to clear setup flag in BOTH collections
                                    val db = FirestoreManager.db
                                    
                                    // 1. Update in Company collection
                                    val staffSnapshot = db.collection("companies").document(UserSession.companyId)
                                        .collection("staff")
                                        .where { "email" equalTo UserSession.userEmail!! }
                                        .get()
                                    
                                    if (staffSnapshot.documents.isNotEmpty()) {
                                        val doc = staffSnapshot.documents.first()
                                        db.collection("companies").document(UserSession.companyId)
                                            .collection("staff").document(doc.id)
                                            .update("setupComplete" to true)
                                    }

                                    // 2. Update in Global collection (Critical for next login)
                                    val globalStaffSnapshot = db.collection("staff")
                                        .where { "email" equalTo UserSession.userEmail!! }
                                        .get()
                                    
                                    if (globalStaffSnapshot.documents.isNotEmpty()) {
                                        val doc = globalStaffSnapshot.documents.first()
                                        db.collection("staff").document(doc.id)
                                            .update("setupComplete" to true)
                                    }
                                    
                                    onComplete()
                                } catch (e: Exception) {
                                    println("DEBUG: Setup Error: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(if (currentStep < totalSteps) "NEXT" else "SAVE & FINISH")
                }
            }
        }
    }
}

private fun parseHexToColor(colorString: String): Color {
    return try {
        if (colorString.startsWith("#")) {
            val hex = colorString.substring(1)
            val fullHex = if (hex.length == 6) "FF$hex" else hex
            Color(fullHex.toLong(16))
        } else {
            Color(0xFF0D47A1)
        }
    } catch (e: Exception) {
        Color(0xFF0D47A1)
    }
}
