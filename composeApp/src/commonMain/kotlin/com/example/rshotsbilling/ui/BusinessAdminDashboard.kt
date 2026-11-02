package com.example.rshotsbilling.ui

import androidx.compose.foundation.background
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
import com.example.rshotsbilling.model.StaffMember
import com.example.rshotsbilling.model.InstallationProfile
import com.example.rshotsbilling.repository.CompanyRepository
import com.example.rshotsbilling.repository.StaffRepository
import com.example.rshotsbilling.repository.DeviceRepository
import com.example.rshotsbilling.utils.UserRole
import com.example.rshotsbilling.utils.UserSession
import kotlinx.coroutines.launch
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessAdminDashboard(onBack: () -> Unit) {
    if (UserSession.currentRole != UserRole.ADMIN && UserSession.currentRole != UserRole.CEO) {
        AccessDeniedScreen(onBack)
        return
    }

    val companyRepository = remember { CompanyRepository() }
    val staffRepository = remember { StaffRepository() }
    val deviceRepository = remember { DeviceRepository() }
    val auditRepository = remember { com.example.rshotsbilling.repository.AuditRepository() }
    
    var company by remember { mutableStateOf<Company?>(null) }
    var staffList by remember { mutableStateOf<List<StaffMember>>(emptyList()) }
    var devices by remember { mutableStateOf<List<InstallationProfile>>(emptyList()) }
    var auditLogs by remember { mutableStateOf<List<com.example.rshotsbilling.model.AuditLog>>(emptyList()) }
    
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var deviceToRename by remember { mutableStateOf<InstallationProfile?>(null) }

    LaunchedEffect(UserSession.businessId) {
        if (UserSession.businessId.isNotEmpty()) {
            company = companyRepository.getCompany(UserSession.businessId)
            scope.launch {
                staffRepository.getStaffListFlow().collect { staffList = it }
            }
            scope.launch {
                deviceRepository.getDevicesForCompanyFlow(UserSession.businessId).collect { devices = it }
            }
            scope.launch {
                auditRepository.getAuditLogsFlow().collect { auditLogs = it }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Admin - ${company?.name ?: "Loading..."}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Brush, null) }, label = { Text("Branding") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.People, null) }, label = { Text("Staff") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Devices, null) }, label = { Text("Devices") })
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("Audit") })
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> BrandingSettings(company) { updated -> 
                    scope.launch { companyRepository.updateCompany(updated); company = updated }
                }
                1 -> StaffManagement(staffList) { showAddStaffDialog = true }
                2 -> CompanyDeviceList(devices) { id, _ ->
                    deviceToRename = devices.find { it.installationId == id }
                }
                3 -> AuditLogList(auditLogs)
            }
        }
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onConfirm = { newStaff ->
                scope.launch { staffRepository.addStaff(newStaff) }
                showAddStaffDialog = false
            }
        )
    }

    if (deviceToRename != null) {
        RenameDeviceDialog(
            device = deviceToRename!!,
            onDismiss = { deviceToRename = null },
            onConfirm = { newName ->
                scope.launch { deviceRepository.renameDevice(deviceToRename!!.installationId, newName) }
                deviceToRename = null
            }
        )
    }
}

@Composable
fun BrandingSettings(company: Company?, onUpdate: (Company) -> Unit) {
    if (company == null) return
    var primaryColor by remember { mutableStateOf(company.theme.primaryColor) }
    var accentColor by remember { mutableStateOf(company.theme.accentColor) }
    var header by remember { mutableStateOf(company.theme.receiptHeader) }
    var footer by remember { mutableStateOf(company.theme.receiptFooter) }
    
    var motto by remember { mutableStateOf(company.companyMotto) }
    var website by remember { mutableStateOf(company.website) }
    var logoUrl by remember { mutableStateOf(company.logo) }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Visual Branding", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("Logo URL") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = motto, onValueChange = { motto = it }, label = { Text("Company Motto") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Website") }, modifier = Modifier.fillMaxWidth())
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = primaryColor, onValueChange = { primaryColor = it }, label = { Text("Primary Color") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = accentColor, onValueChange = { accentColor = it }, label = { Text("Accent Color") }, modifier = Modifier.weight(1f))
        }
        
        Spacer(Modifier.height(16.dp))
        Text("Receipt Customization", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = header, onValueChange = { header = it }, label = { Text("Receipt Header") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = footer, onValueChange = { footer = it }, label = { Text("Receipt Footer") }, modifier = Modifier.fillMaxWidth())
        
        Button(
            onClick = {
                onUpdate(company.copy(
                    logo = logoUrl,
                    companyMotto = motto,
                    website = website,
                    theme = company.theme.copy(
                        primaryColor = primaryColor,
                        accentColor = accentColor,
                        receiptHeader = header,
                        receiptFooter = footer
                    )
                ))
            },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Save Branding")
        }
        
        Spacer(Modifier.height(24.dp))
        Text("Preview", style = MaterialTheme.typography.titleSmall)
        Box(Modifier.size(100.dp, 40.dp).background(parseColor(primaryColor)).padding(8.dp), contentAlignment = Alignment.Center) {
            Text("Primary", color = Color.White)
        }
    }
}

@Composable
fun StaffManagement(staffList: List<StaffMember>, onAddStaff: () -> Unit) {
    var generatedCode by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val repository = remember { CompanyRepository() }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Button(onClick = onAddStaff, Modifier.padding(16.dp).fillMaxWidth()) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Staff")
            }
        }
        items(staffList) { staff ->
            ListItem(
                headlineContent = { Text(staff.displayName) },
                supportingContent = { Text("${staff.role} | ${staff.email}") },
                trailingContent = {
                    TextButton(onClick = { 
                        scope.launch {
                            generatedCode = repository.generateStaffActivationCode(UserSession.businessId)
                        }
                    }) { Text("GENERATE CODE") }
                }
            )
        }
    }

    if (generatedCode != null) {
        AlertDialog(
            onDismissRequest = { generatedCode = null },
            title = { Text("Staff Activation Code") },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Share this code with the staff member:", fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(generatedCode!!, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = { Button(onClick = { generatedCode = null }) { Text("DONE") } }
        )
    }
}

@Composable
fun CompanyDeviceList(devices: List<InstallationProfile>, onRename: (String, String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(devices) { device ->
            ListItem(
                headlineContent = { Text(device.deviceName) },
                supportingContent = { Text("${device.platform} | Last Sync: ${device.lastSync}") },
                trailingContent = {
                    IconButton(onClick = { onRename(device.installationId, device.deviceName) }) { Icon(Icons.Default.Edit, null) }
                }
            )
        }
    }
}

@Composable
fun RenameDeviceDialog(device: InstallationProfile, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newName by remember { mutableStateOf(device.deviceName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Device") },
        text = {
            OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Device Name") }, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            Button(onClick = { onConfirm(newName) }) { Text("RENAME") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (StaffMember) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var staffRole by remember { mutableStateOf(UserRole.SALES_STAFF) }
    var expandedRole by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isCreating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = { Text("Add New Staff") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Temporary Password") }, modifier = Modifier.fillMaxWidth())
                
                Box {
                    OutlinedButton(onClick = { expandedRole = true }, modifier = Modifier.fillMaxWidth()) { Text(staffRole.name) }
                    DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        UserRole.entries.filter { UserSession.currentRole.canManage(it) }.forEach { role -> 
                            DropdownMenuItem(text = { Text(role.name) }, onClick = { staffRole = role; expandedRole = false }) 
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (email.isNotEmpty() && password.isNotEmpty() && displayName.isNotEmpty()) {
                    isCreating = true
                    scope.launch {
                        try {
                            val result = dev.gitlive.firebase.Firebase.auth.createUserWithEmailAndPassword(email, password)
                            val uid = result.user?.uid ?: ""
                            val newStaff = StaffMember(
                                id = uid, 
                                businessId = UserSession.businessId, 
                                email = email, 
                                displayName = displayName, 
                                role = staffRole.name,
                                dateAdded = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                            )
                            onConfirm(newStaff)
                        } catch (e: Exception) {
                            isCreating = false
                        }
                    }
                }
            }, enabled = !isCreating) { Text("CREATE") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isCreating) { Text("CANCEL") } }
    )
}

@Composable
fun BusinessDetails(company: Company?, onUpdate: (Company) -> Unit) {
    if (company == null) return
    var phone by remember { mutableStateOf<String>(company.phone) }
    var address by remember { mutableStateOf<String>(company.physicalAddress) }
    var kraPin by remember { mutableStateOf<String>(company.kraPin) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(value = phone, onValueChange = { newValue -> phone = newValue }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = { newValue -> address = newValue }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = kraPin, onValueChange = { newValue -> kraPin = newValue }, label = { Text("KRA PIN") }, modifier = Modifier.fillMaxWidth())
        
        Button(
            onClick = { onUpdate(company.copy(phone = phone, physicalAddress = address, kraPin = kraPin)) },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Update Details")
        }
    }
}

fun parseColor(hex: String): Color {
    return try {
        val s = hex.removePrefix("#")
        val l = if (s.length == 6) "FF$s".toLong(16) else s.toLong(16)
        Color(l)
    } catch (e: Exception) {
        Color.Blue
    }
}
