@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.rshotsbilling.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.UserRole
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.viewmodel.*
import com.example.rshotsbilling.model.*
import kotlinx.coroutines.launch
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import kotlinx.datetime.Clock

@Composable
fun SettingsScreen(
    staffViewModel: StaffViewModel,
    reportsViewModel: ReportsViewModel,
    messagingViewModel: MessagingViewModel,
    salesViewModel: SalesViewModel,
    purchaseViewModel: PurchaseViewModel,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    if (UserSession.userEmail == null) {
        AccessDeniedScreen(onBack)
        return
    }

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showPurgeConfirm by remember { mutableStateOf(false) }
    var showEndShiftDialog by remember { mutableStateOf(false) }
    var showDeviceManager by remember { mutableStateOf(false) }
    var showResetRequests by remember { mutableStateOf(false) }
    
    var staffToDelete by remember { mutableStateOf<StaffMember?>(null) }
    var staffToEdit by remember { mutableStateOf<StaffMember?>(null) }
    var staffSearchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val staffList by staffViewModel.staffList.collectAsState()
    val resetRequests by staffViewModel.resetRequests.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        staffViewModel.loadStaff()
        staffViewModel.observeResetRequests()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Application Administration", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // SECTION: MY ACCOUNT
            Text("My Account", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)), modifier = Modifier.fillMaxWidth()) {
                var isEditingName by remember { mutableStateOf(false) }
                var newName by remember { mutableStateOf(UserSession.displayName ?: "") }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            if (isEditingName) {
                                OutlinedTextField(value = newName, onValueChange = { newName = it }, modifier = Modifier.weight(1f).height(50.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                                IconButton(onClick = {
                                    UserSession.displayName = newName
                                    val repo = com.example.rshotsbilling.repository.StaffRepository()
                                    scope.launch {
                                        try {
                                            val currentStaff = repo.getStaffList().find { it.email == UserSession.userEmail }
                                            if (currentStaff != null) {
                                                repo.addStaff(currentStaff.copy(displayName = newName))
                                            }
                                        } catch (e: Exception) {}
                                    }
                                    isEditingName = false
                                }) { Icon(Icons.Default.Check, null, tint = Color.Green) }
                            } else {
                                Text(UserSession.displayName ?: "No Name Set", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { isEditingName = true }) { Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(UserSession.userEmail ?: "Not Signed In", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        val isCEO = UserSession.userEmail?.equals("rauphinkelvin8@gmail.com", true) == true
                        Surface(color = when { isCEO -> Color.Red; UserSession.currentRole == UserRole.ADMIN -> Color(0xFFFFD600); UserSession.currentRole == UserRole.MANAGER -> Color.Magenta; else -> Color(0xFF00E5FF) }, shape = MaterialTheme.shapes.extraSmall) {
                            Text(if(isCEO) "CEO / ADMIN" else UserSession.currentRole.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = if (isCEO) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // SECTION: APPLICATION CONFIGURATION
            if (UserSession.currentRole.isAtLeast(UserRole.MANAGER)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, null, tint = Color.Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Application Configuration", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        var name by remember { mutableStateOf(UserSession.companyName) }
                        var moto by remember { mutableStateOf(UserSession.companyMotto) }
                        var logo by remember { mutableStateOf(UserSession.companyLogo) }
                        var phone by remember { mutableStateOf(UserSession.companyPhone) }
                        var email by remember { mutableStateOf(UserSession.companyEmail) }
                        var address by remember { mutableStateOf(UserSession.companyAddress) }
                        var website by remember { mutableStateOf(UserSession.companyWebsite) }
                        
                        var invPrefix by remember { mutableStateOf(UserSession.invoicePrefix) }
                        var recPrefix by remember { mutableStateOf(UserSession.receiptPrefix) }
                        
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = moto, onValueChange = { moto = it }, label = { Text("Tagline / Motto") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = logo, onValueChange = { logo = it }, label = { Text("Brand Logo URL") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = invPrefix, onValueChange = { invPrefix = it }, label = { Text("Invoice Prefix") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            OutlinedTextField(value = recPrefix, onValueChange = { recPrefix = it }, label = { Text("Receipt Prefix") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        }

                        if (UserSession.currentRole.isAtLeast(UserRole.ADMIN)) {
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Business Email") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val repo = com.example.rshotsbilling.repository.CompanyRepository()
                                        val current = repo.getCompany(UserSession.companyId)
                                        if (current != null) {
                                            val updated = current.copy(
                                                name = name,
                                                companyMotto = moto,
                                                logo = logo,
                                                phone = phone,
                                                email = email,
                                                invoicePrefix = invPrefix,
                                                receiptPrefix = recPrefix
                                            )
                                            repo.updateCompany(updated)
                                            UserSession.updateFromCompany(updated)
                                        }
                                    } catch (e: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("SAVE BUSINESS SETTINGS")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SECTION: USERS & ACCESS
            if (UserSession.currentRole.isAtLeast(UserRole.MANAGER)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = Color(0xFFFFD600))
                    Spacer(Modifier.width(8.dp))
                    Text("Users & Access Control", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (UserSession.currentRole.isAtLeast(UserRole.ADMIN)) {
                            Button(onClick = { showAddStaffDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                                Icon(Icons.Default.PersonAdd, null); Spacer(Modifier.width(8.dp)); Text("REGISTER NEW USER")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        Button(onClick = { showBroadcastDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.PrimaryColor, contentColor = Color.White)) {
                            Icon(Icons.Default.Campaign, null); Spacer(Modifier.width(8.dp)); Text("GLOBAL BROADCAST")
                        }
                        
                        if (UserSession.currentRole.isAtLeast(UserRole.ADMIN)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showDeviceManager = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)) {
                                Icon(Icons.Default.Devices, null); Spacer(Modifier.width(8.dp)); Text("MANAGE DEVICES")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Application Users", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            IconButton(onClick = { staffViewModel.loadStaff() }) { Icon(Icons.Default.Refresh, "Refresh", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp)) }
                        }
                        OutlinedTextField(value = staffSearchQuery, onValueChange = { staffSearchQuery = it }, modifier = Modifier.fillMaxWidth().height(48.dp), placeholder = { Text("Search users...", fontSize = 12.sp, color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = Color.Gray) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val filteredStaff = staffList.filter { (it.email.contains(staffSearchQuery, ignoreCase = true) || it.displayName.contains(staffSearchQuery, ignoreCase = true)) }
                        filteredStaff.forEach { staff ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Surface(color = BusinessConfig.PrimaryColor.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape, modifier = Modifier.size(36.dp)) {
                                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                                            }
                                            Spacer(Modifier.width(12.dp)); Column { Text(staff.displayName.ifEmpty { staff.email.split("@")[0] }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(staff.email, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) }
                                        }
                                        
                                        if (UserSession.currentRole.canManage(staff.toUserRole()) || UserSession.userEmail == staff.email) {
                                            Row {
                                                IconButton(onClick = { staffToEdit = staff }) { Icon(Icons.Default.Settings, "Edit Rights", tint = Color.Cyan, modifier = Modifier.size(20.dp)) }
                                                if (UserSession.currentRole.isAtLeast(UserRole.ADMIN) && !staff.email.equals(UserSession.userEmail, true)) {
                                                    IconButton(onClick = { staffToDelete = staff }) { Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp)) }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Rights:", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp); Spacer(Modifier.width(8.dp))
                                        val rightIcons = listOf(UserPermissions.CAN_SELL to Icons.Default.ShoppingCart, UserPermissions.CAN_BUY to Icons.Default.Inventory, UserPermissions.CAN_VIEW_REPORTS to Icons.Default.BarChart, UserPermissions.CAN_APPROVE to Icons.Default.FactCheck, UserPermissions.CAN_MANAGE_SYSTEM to Icons.Default.Settings, UserPermissions.CAN_MANAGE_RECEIPTS to Icons.Default.Receipt, UserPermissions.CAN_MANAGE_INVENTORY to Icons.Default.Inventory2, UserPermissions.CAN_VIEW_COMPLIANCE to Icons.Default.CloudSync, UserPermissions.CAN_MANAGE_MPESA to Icons.Default.Smartphone)
                                        rightIcons.forEach { (perm, icon) -> if (staff.permissions.contains(perm)) { Icon(icon, null, tint = Color.Cyan.copy(alpha = 0.7f), modifier = Modifier.size(12.dp).padding(horizontal = 2.dp)) } }
                                        Spacer(Modifier.weight(1f))
                                        val isStaffCEO = staff.email.equals("rauphinkelvin8@gmail.com", ignoreCase = true)
                                        Surface(color = when { isStaffCEO -> Color.Red; staff.toUserRole() == UserRole.CEO -> Color.Red; staff.toUserRole() == UserRole.ADMIN -> Color(0xFFFFD600); staff.toUserRole() == UserRole.MANAGER -> Color.Magenta; staff.toUserRole() == UserRole.SUPERVISOR -> Color.Blue; else -> Color(0xFF00E5FF) }, shape = RoundedCornerShape(2.dp)) {
                                            Text(if (isStaffCEO) "CEO / ADMIN" else staff.role, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), color = if(isStaffCEO || staff.toUserRole() == UserRole.CEO) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SECTION: SECURITY & AUDIT
            Text("Security & Administrative Audits", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (UserSession.currentRole.isAtLeast(UserRole.ADMIN)) {
                        Button(onClick = { showResetRequests = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (resetRequests.isNotEmpty()) Color.Red else Color.White.copy(alpha = 0.2f))) {
                            Icon(Icons.Default.Password, null); Spacer(Modifier.width(8.dp)); Text("PASSWORD RESET REQUESTS (${resetRequests.size})")
                        }
                    }

                    Button(onClick = { showChangePasswordDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))) {
                        Icon(Icons.Default.LockReset, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("CHANGE MY PASSWORD", color = Color.White)
                    }

                    Button(onClick = { showEndShiftDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.ErrorColor)) {
                        Icon(Icons.Default.Replay, null); Spacer(Modifier.width(8.dp)); Text("END SHIFT & RECONCILE", fontWeight = FontWeight.Bold)
                    }

                    Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))) {
                        Icon(Icons.Default.Logout, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("LOG OUT SESSION", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- DIALOGS ---

    if (showResetRequests) {
        AlertDialog(
            onDismissRequest = { showResetRequests = false },
            containerColor = Color.White,
            title = { Text("Password Reset Requests", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (resetRequests.isEmpty()) {
                        Text("No pending requests.", color = Color.Gray)
                    }
                    resetRequests.forEach { req ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(req.email, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text("Requested: ${Clock.System.now().toEpochMilliseconds() - req.requestedAt}ms ago", fontSize = 10.sp, color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = { staffViewModel.approveResetRequest(req) }) { Icon(Icons.Default.Check, "Approve", tint = Color.Green) }
                                    IconButton(onClick = { staffViewModel.rejectResetRequest(req) }) { Icon(Icons.Default.Close, "Reject", tint = Color.Red) }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showResetRequests = false }) { Text("CLOSE") } }
        )
    }

    if (showEndShiftDialog) {
        var cashCount by remember { mutableStateOf("") }
        var mpesaCount by remember { mutableStateOf("") }
        var bankCount by remember { mutableStateOf("") }
        var isClosing by remember { mutableStateOf(false) }
        val salesToday = salesViewModel.sales.collectAsState().value
        val expensesToday = purchaseViewModel.purchases.collectAsState().value
        val totalRevenue = salesToday.filter { it.status != "REJECTED" }.sumOf { it.total }
        val totalExpenses = expensesToday.filter { it.status != "REJECTED" }.sumOf { it.totalAmount }

        AlertDialog(
            onDismissRequest = { if(!isClosing) showEndShiftDialog = false },
            containerColor = Color.White,
            title = { Text("Daily Reconciliation", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = Color.LightGray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("DAY SUMMARY", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Total Sales:", fontSize = 13.sp, color = Color.Black); Text("${BusinessConfig.CURRENCY} $totalRevenue", fontWeight = FontWeight.Bold, color = Color.Black) }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Total Expenses:", fontSize = 13.sp, color = Color.Black); Text("${BusinessConfig.CURRENCY} $totalExpenses", fontWeight = FontWeight.Bold, color = Color.Black) }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Net Expected:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black); Text("${BusinessConfig.CURRENCY} ${totalRevenue - totalExpenses}", color = BusinessConfig.SuccessColor, fontWeight = FontWeight.ExtraBold) }
                        }
                    }
                    Text("Please enter final counts before logging out.", fontSize = 12.sp, color = Color.Black)
                    OutlinedTextField(value = cashCount, onValueChange = { cashCount = it }, label = { Text("Physical Cash Count") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mpesaCount, onValueChange = { mpesaCount = it }, label = { Text("M-Pesa Balance") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bankCount, onValueChange = { bankCount = it }, label = { Text("Bank Statement Balance") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    isClosing = true
                    val summary = "SHIFT CLOSED\nUser: ${UserSession.userEmail}\n\nSALES: $totalRevenue\nEXPENSES: $totalExpenses\n\nPHYSICAL RECONCILIATION:\nCash: $cashCount\nMpesa: $mpesaCount\nBank: $bankCount"
                    messagingViewModel.createTask(StaffTaskUpgrade(creatorEmail = UserSession.userEmail ?: "SYSTEM", assigneeEmail = "rauphinkelvin8@gmail.com", title = "SHIFT CLOSED: ${UserSession.displayName}", description = summary, priority = TaskPriority.URGENT, status = TaskStatus.PENDING))
                    scope.launch { kotlinx.coroutines.delay(1500); onLogout() }
                }, enabled = !isClosing && cashCount.isNotEmpty()) { Text("CLOSE SHIFT") }
            },
            dismissButton = { TextButton(onClick = { showEndShiftDialog = false }, enabled = !isClosing) { Text("CANCEL") } }
        )
    }

    if (showAddStaffDialog) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var displayName by remember { mutableStateOf("") }
        var initialDeviceSerial by remember { mutableStateOf("") }
        var staffRole by remember { mutableStateOf(UserRole.SALES_STAFF) }
        val selectedRights = remember { mutableStateListOf<String>() }
        var expandedRole by remember { mutableStateOf(false) }
        var isCreating by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isCreating) showAddStaffDialog = false },
            containerColor = Color.White,
            title = { Text("Register New Staff", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Temporary Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(value = initialDeviceSerial, onValueChange = { initialDeviceSerial = it }, label = { Text("Initial Device Serial (Optional)") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. LAPTOP-SERIAL-123", color = Color.Gray) })

                    Text("Designation", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.Black)
                    Box {
                        OutlinedButton(onClick = { expandedRole = true }, modifier = Modifier.fillMaxWidth()) { Text(staffRole.name) }
                        DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                            UserRole.entries.filter { UserSession.currentRole.canManage(it) }.forEach { role -> 
                                DropdownMenuItem(text = { Text(role.name) }, onClick = { staffRole = role; expandedRole = false }) 
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp)); Text("Access Rights", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                    val perms = listOf("Record Sales" to UserPermissions.CAN_SELL, "Record Expenses" to UserPermissions.CAN_BUY, "Inventory Hub" to UserPermissions.CAN_MANAGE_INVENTORY, "Financial Reports" to UserPermissions.CAN_VIEW_REPORTS, "Compliance Hub" to UserPermissions.CAN_VIEW_COMPLIANCE, "M-Pesa Management" to UserPermissions.CAN_MANAGE_MPESA, "Approve Data" to UserPermissions.CAN_APPROVE, "System Management" to UserPermissions.CAN_MANAGE_SYSTEM, "Manage Receipts" to UserPermissions.CAN_MANAGE_RECEIPTS)
                    perms.forEach { (label, perm) -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (selectedRights.contains(perm)) selectedRights.remove(perm) else selectedRights.add(perm) }) { Checkbox(checked = selectedRights.contains(perm), onCheckedChange = null); Text(label, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp), color = Color.Black) } }
                    errorMsg?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && displayName.isNotEmpty()) {
                        isCreating = true
                        scope.launch {
                            try {
                                val result = Firebase.auth.createUserWithEmailAndPassword(email, password)
                                val uid = result.user?.uid ?: ""
                                val devices = if (initialDeviceSerial.isNotBlank()) listOf(initialDeviceSerial) else emptyList()
                                val newStaff = StaffMember(id = uid, businessId = UserSession.businessId, email = email, displayName = displayName, role = staffRole.name, permissions = selectedRights.toList(), registeredDevices = devices, dateAdded = Clock.System.now().toEpochMilliseconds())
                                staffViewModel.addStaff(newStaff)
                                showAddStaffDialog = false
                            } catch (e: Exception) { isCreating = false; errorMsg = e.message }
                        }
                    }
                }, enabled = !isCreating) { Text("CREATE ACCOUNT") }
            },
            dismissButton = { TextButton(onClick = { showAddStaffDialog = false }, enabled = !isCreating) { Text("CANCEL") } }
        )
    }

    if (staffToDelete != null) {
        AlertDialog(
            onDismissRequest = { staffToDelete = null },
            containerColor = Color.White,
            title = { Text("Remove Staff Member?", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text("Are you sure you want to remove ${staffToDelete?.email}? This will revoke their access immediately.", color = Color.Black) },
            confirmButton = {
                Button(onClick = { staffViewModel.deleteStaff(staffToDelete!!.id); staffToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("DELETE STAFF", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { staffToDelete = null }) { Text("CANCEL") } }
        )
    }

    if (staffToEdit != null) {
        EditStaffRightsDialog(
            staff = staffToEdit!!,
            onDismiss = { staffToEdit = null },
            onSave = { updatedStaff ->
                staffViewModel.addStaff(updatedStaff)
                staffToEdit = null
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showDeviceManager) {
        DeviceManagerDialog(staffList, staffViewModel) { showDeviceManager = false }
    }
}

@Composable
fun EditStaffRightsDialog(
    staff: StaffMember,
    onDismiss: () -> Unit,
    onSave: (StaffMember) -> Unit
) {
    var staffRole by remember { mutableStateOf(staff.toUserRole()) }
    val selectedRights = remember { mutableStateListOf<String>().apply { addAll(staff.permissions) } }
    var expandedRole by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Edit Staff Rights: ${staff.displayName.ifEmpty { staff.email }}", fontWeight = FontWeight.Bold, color = Color.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Designation", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.Black)
                Box {
                    OutlinedButton(onClick = { expandedRole = true }, modifier = Modifier.fillMaxWidth()) { Text(staffRole.name) }
                    DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        UserRole.entries.filter { UserSession.currentRole.canManage(it) }.forEach { role -> 
                            DropdownMenuItem(text = { Text(role.name) }, onClick = { staffRole = role; expandedRole = false }) 
                        }
                    }
                }
                Spacer(Modifier.height(8.dp)); Text("Access Rights", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                val perms = listOf("Record Sales" to UserPermissions.CAN_SELL, "Record Expenses" to UserPermissions.CAN_BUY, "Inventory Hub" to UserPermissions.CAN_MANAGE_INVENTORY, "Financial Reports" to UserPermissions.CAN_VIEW_REPORTS, "Compliance Hub" to UserPermissions.CAN_VIEW_COMPLIANCE, "M-Pesa Management" to UserPermissions.CAN_MANAGE_MPESA, "Approve Data" to UserPermissions.CAN_APPROVE, "System Management" to UserPermissions.CAN_MANAGE_SYSTEM, "Manage Receipts" to UserPermissions.CAN_MANAGE_RECEIPTS)
                perms.forEach { (label, perm) -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (selectedRights.contains(perm)) selectedRights.remove(perm) else selectedRights.add(perm) }) { Checkbox(checked = selectedRights.contains(perm), onCheckedChange = null); Text(label, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp), color = Color.Black) } }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(staff.copy(role = staffRole.name, permissions = selectedRights.toList()))
            }) { Text("SAVE CHANGES") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        containerColor = Color.White,
        title = { Text("Change My Password", fontWeight = FontWeight.Bold, color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter a new password for your account.", color = Color.Gray, fontSize = 12.sp)
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm New Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                errorMsg?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                    isUpdating = true
                    scope.launch {
                        try {
                            Firebase.auth.currentUser?.updatePassword(newPassword)
                            onDismiss()
                        } catch (e: Exception) {
                            isUpdating = false
                            errorMsg = e.message
                        }
                    }
                } else if (newPassword != confirmPassword) {
                    errorMsg = "Passwords do not match."
                }
            }, enabled = !isUpdating) { Text("UPDATE PASSWORD") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isUpdating) { Text("CANCEL") } }
    )
}

@Composable
fun DeviceManagerDialog(staffList: List<StaffMember>, viewModel: StaffViewModel, onDismiss: () -> Unit) {
    var selectedStaff by remember { mutableStateOf<StaffMember?>(null) }
    var newSerial by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Manage Registered Devices", fontWeight = FontWeight.Bold, color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text("Select staff to manage their devices:", fontSize = 12.sp, color = Color.Gray)
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedStaff?.email ?: "Select User")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        staffList.forEach { staff ->
                            DropdownMenuItem(text = { Text(staff.email) }, onClick = { selectedStaff = staff; expanded = false })
                        }
                    }
                }

                selectedStaff?.let { staff ->
                    Text("Registered Devices:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    if (staff.registeredDevices.isEmpty()) {
                        Text("No devices registered.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        staff.registeredDevices.forEach { serial ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(serial, fontSize = 12.sp, color = Color.Black)
                                IconButton(onClick = {
                                    val updated = staff.copy(registeredDevices = staff.registeredDevices - serial)
                                    viewModel.addStaff(updated)
                                    selectedStaff = updated
                                }) { Icon(Icons.Default.Delete, "Remove", tint = Color.Red, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Add New Device Serial:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                    OutlinedTextField(value = newSerial, onValueChange = { newSerial = it }, placeholder = { Text("Enter Serial Number") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        if (newSerial.isNotBlank()) {
                            val updated = staff.copy(registeredDevices = staff.registeredDevices + newSerial)
                            viewModel.addStaff(updated)
                            selectedStaff = updated
                            newSerial = ""
                        }
                    }, modifier = Modifier.fillMaxWidth(), enabled = newSerial.isNotBlank()) {
                        Text("REGISTER DEVICE")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("DONE") }
        }
    )
}
