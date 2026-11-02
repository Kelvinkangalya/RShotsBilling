package com.example.rshotsbilling.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.rshotsbilling.model.InstallationProfile
import com.example.rshotsbilling.model.AuditLog
import com.example.rshotsbilling.repository.CompanyRepository
import com.example.rshotsbilling.repository.DeviceRepository
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.launch
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorAdminDashboard(onBack: () -> Unit) {
    val startTime = remember { getCurrentTimestamp() }
    LaunchedEffect(Unit) {
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] VendorAdminDashboard FIRST RENDER: ${getCurrentTimestamp() - startTime} ms after composition start")
    }

    if (UserSession.userEmail?.equals("rauphinkelvin8@gmail.com", true) != true) {
        AccessDeniedScreen(onBack)
        return
    }

    val companyRepository = remember { CompanyRepository() }
    val deviceRepository = remember { DeviceRepository() }
    
    var companies by remember { mutableStateOf<List<Company>>(emptyList()) }
    var devices by remember { mutableStateOf<List<InstallationProfile>>(emptyList()) }
    var auditLogs by remember { mutableStateOf<List<AuditLog>>(emptyList()) }
    
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            companyRepository.getAllCompaniesFlow().collect {
                companies = it
                isLoading = false
            }
        }
        scope.launch {
            deviceRepository.getAllDevicesFlow().collect {
                devices = it
            }
        }
        scope.launch {
            companyRepository.getAuditLogsFlow().collect {
                auditLogs = it
            }
        }
    }

    if (selectedCompany != null) {
        CompanyDetailsView(
            company = selectedCompany!!,
            onBack = { selectedCompany = null },
            onUpdate = { scope.launch { companyRepository.updateCompany(it); selectedCompany = it } }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Company")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Business, null) }, label = { Text("Companies") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Devices, null) }, label = { Text("Devices") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("Audit") })
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Stats") })
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        placeholder = { Text("Search companies...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                }

                when (selectedTab) {
                    0 -> CompanyList(
                        companies.filter { company -> company.name.contains(searchQuery, ignoreCase = true) || company.code.contains(searchQuery, ignoreCase = true) },
                        onSuspend = { id, suspended -> scope.launch { companyRepository.suspendCompany(id, suspended) } },
                        onDelete = { id -> scope.launch { companyRepository.deleteCompany(id) } },
                        onSelect = { selectedCompany = it }
                    )
                    1 -> DeviceList(
                        devices,
                        onRename = { id, name -> scope.launch { deviceRepository.renameDevice(id, name) } },
                        onSetStatus = { id, active -> scope.launch { deviceRepository.setDeviceStatus(id, active) } },
                        onApprove = { id, approved -> scope.launch { deviceRepository.approveDevice(id, approved) } },
                        onDelete = { id -> scope.launch { deviceRepository.deleteDevice(id) } }
                    )
                    2 -> AuditLogList(auditLogs)
                    3 -> SystemStats(companies, devices)
                }
            }
        }
    }

    if (showCreateDialog) {
        CompanyRegistrationWizard(
            onDismiss = { showCreateDialog = false },
            onComplete = {
                showCreateDialog = false
                scope.launch {
                    // Refresh logs/companies happens via flow, but we can log locally
                }
            }
        )
    }
}

@Composable
fun CompanyList(companies: List<Company>, onSuspend: (String, Boolean) -> Unit, onDelete: (String) -> Unit, onSelect: (Company) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(companies) { company ->
            ListItem(
                modifier = Modifier.clickable { onSelect(company) },
                headlineContent = { Text(company.name) },
                supportingContent = { Text("Code: ${company.code} | ${company.subscriptionStatus}") },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onSuspend(company.id, company.active) }) {
                            Icon(
                                if (company.active) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Suspend/Resume",
                                tint = if (company.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = { onDelete(company.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DeviceList(
    devices: List<InstallationProfile>,
    onRename: (String, String) -> Unit,
    onSetStatus: (String, Boolean) -> Unit,
    onApprove: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(devices) { device ->
            ListItem(
                headlineContent = { Text(device.deviceName) },
                supportingContent = { Text("${device.platform} | Company: ${device.businessId}") },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Approved", fontSize = 8.sp)
                            Checkbox(checked = device.approved, onCheckedChange = { onApprove(device.installationId, it) })
                        }
                        Switch(checked = device.active, onCheckedChange = { onSetStatus(device.installationId, it) })
                        IconButton(onClick = { onDelete(device.installationId) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AuditLogList(logs: List<AuditLog>) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(logs.sortedByDescending { it.timestamp }) { log ->
            ListItem(
                headlineContent = { Text(log.action) },
                supportingContent = { Text("${log.userEmail} | ${log.metadata["details"] ?: ""}") },
                trailingContent = { Text(log.timestamp.toString(), fontSize = 10.sp) }
            )
        }
    }
}

@Composable
fun SystemStats(companies: List<Company>, devices: List<InstallationProfile>) {
    Column(Modifier.padding(16.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text("Company Statistics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            SystemStatCard("Total", companies.size.toString())
            SystemStatCard("Active", companies.count { it.active }.toString())
            SystemStatCard("Trial", companies.count { it.subscriptionStatus == "Trial" }.toString())
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            SystemStatCard("Suspended", companies.count { it.subscriptionStatus == "Suspended" }.toString())
            SystemStatCard("Expired", companies.count { it.subscriptionStatus == "Expired" }.toString())
            SystemStatCard("Devices", devices.size.toString())
        }
        
        Spacer(Modifier.height(24.dp))
        Text("Recently Active Companies", style = MaterialTheme.typography.titleMedium)
        companies.sortedByDescending { it.lastLogin }.take(5).forEach { company ->
            ListItem(
                headlineContent = { Text(company.name) },
                supportingContent = { Text("Last Login: ${company.lastLogin}") }
            )
        }
    }
}

@Composable
fun SystemStatCard(label: String, value: String) {
    Card(modifier = Modifier.padding(4.dp).width(110.dp)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsView(company: Company, onBack: () -> Unit, onUpdate: (Company) -> Unit) {
    var showResetDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(company.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Business Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            DetailItem("Company ID", company.id)
            DetailItem("Company Code", company.code)
            DetailItem("Admin Email", company.adminEmail)
            DetailItem("Subscription", company.subscriptionStatus)
            DetailItem("Status", if (company.active) "Active" else "Inactive")
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text("Registration Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            DetailItem("KRA PIN", company.kraPin)
            DetailItem("Address", company.physicalAddress)
            DetailItem("Phone", company.phone)
            DetailItem("Created Date", company.createdDate.toString())
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text("Usage Metrics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            DetailItem("Staff Count", company.staffCount.toString())
            DetailItem("Device Count", company.deviceCount.toString())
            DetailItem("Product Count", company.productCount.toString())
            
            Spacer(Modifier.height(24.dp))
            Button(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("RESET ADMIN PASSWORD")
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Admin Password") },
            text = { Text("Send a password reset email to ${company.adminEmail}?") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            dev.gitlive.firebase.Firebase.auth.sendPasswordResetEmail(company.adminEmail)
                        } catch (e: Exception) {
                            com.example.rshotsbilling.utils.KMPLogger.log("ERROR: Password reset failed: ${e.message}")
                        }
                        showResetDialog = false
                    }
                }) { Text("SEND EMAIL") }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("CANCEL") } }
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
