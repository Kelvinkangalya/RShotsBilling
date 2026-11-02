package com.example.rshotsbilling.ui

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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyRegistrationWizard(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4
    val scope = rememberCoroutineScope()
    val repository = remember { CompanyRepository() }

    // State for all fields
    var name by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var kraPin by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Retail") }
    var country by remember { mutableStateOf("Kenya") }
    var county by remember { mutableStateOf("") }
    var town by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    var receiptHeader by remember { mutableStateOf("") }
    var receiptFooter by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("KES") }
    var timezone by remember { mutableStateOf("Africa/Nairobi") }
    
    var plan by remember { mutableStateOf("Basic") }
    var adminName by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }

    var registrationResult by remember { mutableStateOf<com.example.rshotsbilling.repository.CompanyRepository.RegistrationResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize(),
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(if (registrationResult == null) "Company Registration Wizard - Step $currentStep of $totalSteps" else "Registration Successful") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                        }
                    )
                    
                    if (registrationResult == null) {
                        LinearProgressIndicator(
                            progress = currentStep.toFloat() / totalSteps,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (registrationResult != null) {
                            SuccessScreen(registrationResult!!, onComplete)
                        } else {
                            when (currentStep) {
                                1 -> {
                                    Text("Basic Business Info", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name *") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = regNumber, onValueChange = { regNumber = it }, label = { Text("Registration Number") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = kraPin, onValueChange = { kraPin = it }, label = { Text("KRA PIN *") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = businessType, onValueChange = { businessType = it }, label = { Text("Business Type") }, modifier = Modifier.fillMaxWidth())
                                }
                                2 -> {
                                    Text("Contact & Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = county, onValueChange = { county = it }, label = { Text("County") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = town, onValueChange = { town = it }, label = { Text("Town") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Physical Address") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Business Phone *") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Business Email") }, modifier = Modifier.fillMaxWidth())
                                }
                                3 -> {
                                    Text("Branding & Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(value = receiptHeader, onValueChange = { receiptHeader = it }, label = { Text("Receipt Header") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = receiptFooter, onValueChange = { receiptFooter = it }, label = { Text("Receipt Footer") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency (e.g. KES, USD)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = timezone, onValueChange = { timezone = it }, label = { Text("Timezone") }, modifier = Modifier.fillMaxWidth())
                                }
                                4 -> {
                                    Text("Subscription & Administration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("Select Plan", style = MaterialTheme.typography.labelLarge)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(selected = plan == "Basic", onClick = { plan = "Basic" }, label = { Text("Basic") })
                                        FilterChip(selected = plan == "Standard", onClick = { plan = "Standard" }, label = { Text("Standard") })
                                        FilterChip(selected = plan == "Premium", onClick = { plan = "Premium" }, label = { Text("Premium") })
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(value = adminName, onValueChange = { adminName = it }, label = { Text("Admin Name *") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = adminEmail, onValueChange = { adminEmail = it }, label = { Text("Admin Email *") }, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                        
                        errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    if (registrationResult == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { if (currentStep > 1) currentStep-- else onDismiss() },
                                enabled = !isLoading
                            ) {
                                Text(if (currentStep > 1) "BACK" else "CANCEL")
                            }
                            
                            Button(
                                onClick = {
                                    if (currentStep < totalSteps) {
                                        currentStep++
                                    } else {
                                        if (name.isBlank() || kraPin.isBlank() || phone.isBlank() || adminName.isBlank() || adminEmail.isBlank()) {
                                            errorMessage = "Please fill all required fields (*)"
                                        } else {
                                            isLoading = true
                                            errorMessage = null
                                            scope.launch {
                                                try {
                                                    val company = Company(
                                                        name = name,
                                                        businessRegistrationNumber = regNumber,
                                                        kraPin = kraPin,
                                                        businessType = businessType,
                                                        country = country,
                                                        county = county,
                                                        town = town,
                                                        physicalAddress = address,
                                                        phone = phone,
                                                        email = email,
                                                        theme = CompanyTheme(
                                                            receiptHeader = receiptHeader,
                                                            receiptFooter = receiptFooter,
                                                            currency = currency,
                                                            timezone = timezone
                                                        ),
                                                        subscriptionPlan = plan,
                                                        adminName = adminName,
                                                        adminEmail = adminEmail
                                                    )
                                                    registrationResult = repository.registerNewCompany(company)
                                                } catch (e: Exception) {
                                                    errorMessage = e.message ?: "Failed to register company"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = !isLoading
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                else Text(if (currentStep < totalSteps) "NEXT" else "FINISH & REGISTER")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SuccessScreen(
    result: com.example.rshotsbilling.repository.CompanyRepository.RegistrationResult,
    onComplete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Business Created Successfully!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Share these credentials with the Company Admin.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        CredentialItem("Company ID", result.companyId)
        CredentialItem("Admin Email", result.adminEmail)
        CredentialItem("Temporary Password", result.temporaryPassword)
        CredentialItem("Activation Code", result.activationCode)

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /* Copy Logic */ }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text("COPY")
            }
            OutlinedButton(onClick = { /* Print Logic */ }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Print, null); Spacer(Modifier.width(8.dp)); Text("PRINT")
            }
        }

        Button(onClick = onComplete, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)) {
            Text("DONE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CredentialItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { /* Individual Copy */ }) {
                Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp))
            }
        }
        HorizontalDivider()
    }
}
