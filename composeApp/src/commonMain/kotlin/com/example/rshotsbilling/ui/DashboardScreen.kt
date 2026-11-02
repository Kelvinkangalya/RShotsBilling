package com.example.rshotsbilling.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.ui.components.AnalyticsCard
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.UserRole
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.viewmodel.PurchaseViewModel
import com.example.rshotsbilling.viewmodel.SalesViewModel
import com.example.rshotsbilling.viewmodel.ServiceViewModel
import com.example.rshotsbilling.viewmodel.StaffViewModel
import com.example.rshotsbilling.viewmodel.MessagingViewModel
import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.utils.SyncStatus
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.rshotsbilling.utils.FirestoreManager
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.Firebase

data class DashboardItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun DashboardScreen(
    salesViewModel: SalesViewModel,
    purchaseViewModel: PurchaseViewModel,
    serviceViewModel: ServiceViewModel,
    staffViewModel: StaffViewModel,
    messagingViewModel: MessagingViewModel,
    onNavigate: (String) -> Unit
) {
    // 1. SESSION READINESS GUARD
    if (!UserSession.isSessionReady) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0D47A1)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(Modifier.height(24.dp))
                Text("Finalizing Secure Session...", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Loading branding and business logic", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
        
        // Retry logic if stuck
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(8000)
            if (!UserSession.isSessionReady) {
                com.example.rshotsbilling.utils.KMPLogger.log("WARNING: [Dashboard] Session not ready after 8s. Force starting.")
                UserSession.isSessionReady = true
            }
        }
        return
    }

    val startTime = remember { com.example.rshotsbilling.utils.getCurrentTimestamp() }
    LaunchedEffect(Unit) {
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] DashboardScreen FIRST RENDER: ${com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime} ms after composition start")
    }

    val sales by salesViewModel.sales.collectAsState()
    val purchases by purchaseViewModel.purchases.collectAsState()
    val modernTasks by messagingViewModel.tasks.collectAsState()
    
    val rejectedSales = sales.filter { it.status == "REJECTED" && it.servedBy.equals(UserSession.userEmail, ignoreCase = true) }
    val rejectedPurchases = purchases.filter { it.status == "REJECTED" && it.recordedBy.equals(UserSession.userEmail, ignoreCase = true) }

    var saleToEdit by remember { mutableStateOf<com.example.rshotsbilling.model.Sale?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val totalSales = sales.sumOf { it.total }
    val totalExpenses = purchases.sumOf { it.totalAmount }
    val profit = totalSales - totalExpenses

    val dashboardItems = listOf(
        DashboardItem("Sales & Receipts", "sales_hub", Icons.Default.ShoppingCart),
        DashboardItem("Procurement Hub", "inventory_hub", Icons.Default.Inventory),
        DashboardItem("Services & Customers", "services", Icons.Default.Groups),
        DashboardItem("Business Intelligence", "reports", Icons.Default.BarChart),
        DashboardItem("Task Center", "task_center", Icons.Default.Task),
        DashboardItem("HR & Payroll", "payroll_dashboard", Icons.Default.Payments),
        DashboardItem("Compliance Hub", "compliance_hub", Icons.Default.CloudSync),
        DashboardItem("Vendor Admin", "vendor_dashboard", Icons.Default.AdminPanelSettings)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BRANDING HEADER
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (UserSession.companyLogo.isNotEmpty()) {
                // Placeholder for dynamic logo
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = BusinessConfig.COMPANY_NAME,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // CLOUD STATUS
            val isSyncing by SyncStatus.isSyncing.collectAsState()
            
            val alpha by rememberInfiniteTransition().animateFloat(
                initialValue = 0.4f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(if (isSyncing) Color.Cyan else BusinessConfig.SuccessColor, CircleShape).alpha(alpha))
                Spacer(Modifier.width(6.dp))
                Text(if (isSyncing) "SYNCING..." else "CLOUD ACTIVE", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = { onNavigate("settings") }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.AccountCircle, "Profile", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val isCEO = UserSession.userEmail?.equals("rauphinkelvin8@gmail.com", true) == true
            Surface(color = Color.White.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                Text(text = "${if(isCEO) "CEO/ADMIN" else UserSession.currentRole.name}: ${UserSession.displayName ?: "User"}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(text = "Business Overview", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onNavigate("sales") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor)) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("QUICK SALE")
            }
            Button(onClick = {
                if (UserSession.hasPermission(UserPermissions.CAN_VIEW_REPORTS)) onNavigate("reports")
                else snackbarMessage = "Access Denied: Reports Right Required"
            }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.PrimaryColor)) {
                Icon(Icons.Default.Assessment, null); Spacer(Modifier.width(4.dp)); Text("INSIGHTS")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ALERTS & TASKS
        val activeTasksCount by messagingViewModel.activeTasksCount.collectAsState(0)
        val overdueTasksCount by messagingViewModel.overdueTasksCount.collectAsState(0)

        if (activeTasksCount > 0 || rejectedSales.isNotEmpty() || rejectedPurchases.isNotEmpty()) {
            Text(text = "Attention Required", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

            if (activeTasksCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate("task_center") },
                    colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Task, null, tint = Color.Yellow)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Task Center Status", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("$activeTasksCount Active Tasks â€¢ $overdueTasksCount Overdue", color = Color.Yellow, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }
            }

            rejectedSales.forEach { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cancel, null, tint = Color.Red)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sale Rejected: ${sale.customerName}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Reason: ${sale.auditReason}", color = Color.Yellow, fontSize = 12.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { salesViewModel.deleteSale(sale.id, "Staff deleted rejected sale") }) { Text("DELETE", color = Color.White.copy(alpha = 0.6f)) }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { saleToEdit = sale }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black), shape = RoundedCornerShape(8.dp)) { Text("FIX & RESUBMIT", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }

        if (saleToEdit != null) {
            AlertDialog(onDismissRequest = { saleToEdit = null }, containerColor = Color.White, title = { Text("Fix & Resubmit Sale", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Reason: ${saleToEdit?.auditReason}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Update details and resubmit for audit.", color = Color.Black)
                    }
                },
                confirmButton = { Button(onClick = { saleToEdit = null }, colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor)) { Text("UNDERSTOOD") } }
            )
        }

        // ANALYTICS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsCard(title = "SALES", value = "${BusinessConfig.CURRENCY} ${totalSales.toInt()}", subtitle = "Total Revenue", color = BusinessConfig.SuccessColor, modifier = Modifier.weight(1f))
            AnalyticsCard(title = "EXPENSES", value = "${BusinessConfig.CURRENCY} ${totalExpenses.toInt()}", subtitle = "Total Costs", color = BusinessConfig.ErrorColor, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        AnalyticsCard(title = "NET PROFIT", value = "${BusinessConfig.CURRENCY} ${profit.toInt()}", subtitle = "Overall Earnings", color = if (profit >= 0) BusinessConfig.SuccessColor else BusinessConfig.ErrorColor, modifier = Modifier.fillMaxWidth())

        // MANAGEMENT MENU
        Text(text = "Management Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false,
            modifier = Modifier.heightIn(max = 600.dp)
        ) {
            items(dashboardItems) { item ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.96f else 1f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(),
                            onClick = {
                                val perm = when(item.route) {
                                    "sales_hub" -> UserPermissions.CAN_SELL
                                    "services" -> UserPermissions.CAN_SELL
                                    "inventory_hub" -> UserPermissions.CAN_MANAGE_INVENTORY
                                    "reports" -> UserPermissions.CAN_VIEW_REPORTS
                                    "compliance_hub" -> UserPermissions.CAN_VIEW_COMPLIANCE
                                    "task_center" -> UserPermissions.CAN_SELL
                                    "payroll_dashboard" -> UserPermissions.CAN_MANAGE_PAYROLL
                                    "vendor_dashboard" -> ""
                                    else -> ""
                                }
                                
                                if (item.route == "vendor_dashboard") {
                                    if (UserSession.userEmail?.equals("rauphinkelvin8@gmail.com", true) == true) onNavigate(item.route)
                                    else snackbarMessage = "Access Denied: Owner Only"
                                } else if (UserSession.hasPermission(perm)) {
                                    onNavigate(item.route)
                                } else {
                                    snackbarMessage = "Access Denied: Missing Rights"
                                }
                            }
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = item.title, 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // OPTION 2: END SHIFT (Formal closing with summary)
            Button(
                onClick = { onNavigate("settings") }, // End shift is now anchored in the management hub for security
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
            ) {
                Icon(Icons.Default.Lock, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("END SHIFT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { 
                    UserSession.clear()
                    onNavigate("login") 
                },
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("LOG OUT", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    snackbarMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { snackbarMessage = null },
            title = { Text("Information") },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = { snackbarMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

