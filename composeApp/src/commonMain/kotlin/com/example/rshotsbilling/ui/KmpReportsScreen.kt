@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.rshotsbilling.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.ui.components.AnalyticsCard
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.UserRole
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.viewmodel.PurchaseViewModel
import com.example.rshotsbilling.viewmodel.ReportsViewModel
import com.example.rshotsbilling.viewmodel.SalesViewModel
import com.example.rshotsbilling.viewmodel.ReceiptViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue

@Composable
fun KmpReportsScreen(
    salesViewModel: SalesViewModel,
    purchaseViewModel: PurchaseViewModel,
    receiptViewModel: ReceiptViewModel? = null,
    reportsViewModel: ReportsViewModel,
    onBack: () -> Unit
) {
    if (!UserSession.hasPermission(UserPermissions.CAN_VIEW_REPORTS)) {
        AccessDeniedScreen(onBack)
        return
    }

    // VIEWMODEL STATES
    val summary by reportsViewModel.summary.collectAsState()
    val topProducts by reportsViewModel.topProducts.collectAsState()
    val profitTrend by reportsViewModel.profitTrend.collectAsState()
    val filteredSales by reportsViewModel.filteredSales.collectAsState()
    val filteredPurchases by reportsViewModel.filteredPurchases.collectAsState()
    val lowStockAlerts by reportsViewModel.lowStockAlerts.collectAsState()
    val inventoryMovement by reportsViewModel.inventoryMovement.collectAsState()
    val staffPerformance by reportsViewModel.staffPerformance.collectAsState()
    val hasPending by reportsViewModel.hasPendingApprovals.collectAsState()

    // TREND CALCULATION
    val trendPercent = remember(filteredSales) {
        try {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val yesterdayStart = now - 86400000
            val yesterdaySales = filteredSales.filter { it.timestamp in (yesterdayStart - 86400000)..yesterdayStart }.sumOf { it.total }
            val todaySales = filteredSales.filter { it.timestamp >= yesterdayStart }.sumOf { it.total }
            if (yesterdaySales > 0) ((todaySales - yesterdaySales) / yesterdaySales * 100).toInt() else 0
        } catch (e: Exception) { 0 }
    }

    // UI CONTROLS
    var showApprovalPanel by remember { mutableStateOf(false) }
    var showWipeConfirm by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // AUDIT STATES
    var auditItemToReview by remember { mutableStateOf<Pair<String, String>?>(null) }
    var auditReason by remember { mutableStateOf("") }
    var isApproving by remember { mutableStateOf(true) }

    // DATE FILTER STATES
    var fromDate by remember { mutableStateOf(kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - (30L * 24 * 60 * 60 * 1000)) } // 30 days ago
    var toDate by remember { mutableStateOf(kotlinx.datetime.Clock.System.now().toEpochMilliseconds()) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    LaunchedEffect(fromDate, toDate) {
        reportsViewModel.loadReportData(fromDate, toDate)
    }

    val cashSales = filteredSales.filter { it.paymentMethod.equals("Cash", ignoreCase = true) }.sumOf { it.total }
    val mpesaSales = filteredSales.filter { it.paymentMethod.equals("M-Pesa", ignoreCase = true) }.sumOf { it.total }
    val bankSales = filteredSales.filter { it.paymentMethod.contains("Bank", ignoreCase = true) }.sumOf { it.total }
    
    val etimsSalesTotal = filteredSales.filter { it.isEtims }.sumOf { it.total }
    val nonEtimsSalesTotal = filteredSales.filter { !it.isEtims }.sumOf { it.total }
    val taxCollected = filteredSales.filter { it.isEtims }.sumOf { it.taxAmount }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Business Intelligence Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { reportsViewModel.loadReportData(fromDate, toDate) }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // DATE RANGE SELECTOR
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val fromStr = kotlinx.datetime.Instant.fromEpochMilliseconds(fromDate).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()
                val toStr = kotlinx.datetime.Instant.fromEpochMilliseconds(toDate).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()
                
                DateCard("FROM", fromStr, Modifier.weight(1f)) { showFromPicker = true }
                DateCard("TO", toStr, Modifier.weight(1f)) { showToPicker = true }
            }

            if (hasPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color.Yellow)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "WARNING: Some transactions are PENDING APPROVAL.",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. SUMMARY DASHBOARD
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalyticsCard(
                    title = "REVENUE", 
                    value = "${BusinessConfig.CURRENCY} ${summary.totalSales.toInt()}", 
                    subtitle = if(trendPercent >= 0) "↑ $trendPercent% vs Yesterday" else "↓ ${trendPercent.absoluteValue}% vs Yesterday",
                    color = BusinessConfig.SuccessColor, 
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "COSTS", 
                    value = "${BusinessConfig.CURRENCY} ${summary.totalExpenses.toInt()}", 
                    subtitle = "Expenses Total", 
                    color = BusinessConfig.ErrorColor, 
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // FUND PARTITIONING
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CASH", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text("${BusinessConfig.CURRENCY} ${cashSales.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("M-PESA", color = Color.Cyan.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text("${BusinessConfig.CURRENCY} ${mpesaSales.toInt()}", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BANK", color = Color.Yellow.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text("${BusinessConfig.CURRENCY} ${bankSales.toInt()}", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnalyticsCard(
                title = "NET OPERATING PROFIT", 
                value = "${BusinessConfig.CURRENCY} ${summary.profit.toInt()}", 
                subtitle = "Actual Earnings", 
                color = if (summary.profit >= 0) BusinessConfig.SuccessColor else BusinessConfig.ErrorColor, 
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // eTIMS COMPLIANCE SUMMARY
            Text("Tax Compliance Overview", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("eTIMS Sales:", color = Color.White.copy(alpha = 0.7f))
                        Text("${BusinessConfig.CURRENCY} ${etimsSalesTotal.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Normal Sales:", color = Color.White.copy(alpha = 0.7f))
                        Text("${BusinessConfig.CURRENCY} ${nonEtimsSalesTotal.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TAX COLLECTED (VAT):", color = BusinessConfig.WarningColor, fontWeight = FontWeight.Bold)
                        Text("${BusinessConfig.CURRENCY} ${taxCollected.toInt()}", color = BusinessConfig.WarningColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. OPERATIONAL ALERTS
            if (lowStockAlerts.isNotEmpty()) {
                Text("Operational Alerts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BusinessConfig.WarningColor.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, BusinessConfig.WarningColor.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        lowStockAlerts.forEach { alert ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.Warning, null, tint = BusinessConfig.WarningColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${alert.itemName} is reaching critical level (${alert.currentQty} left)", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            // 4. GROWTH ANALYSIS CHART
            Text("Revenue Trend Analysis", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                if (profitTrend.isNotEmpty()) {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column {
                            Text("Movement Summary:", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            profitTrend.forEach { (date, revenue) ->
                                Text("$date: ${BusinessConfig.CURRENCY} ${revenue.toInt()}", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No movement data for this range.", color = Color.White.copy(alpha = 0.4f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. INVENTORY VELOCITY
            Text("Inventory & Service Velocity", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Service Name", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                        Text("Sold", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                        Text("Current Stock", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                    }
                    Spacer(Modifier.height(8.dp))
                    if (inventoryMovement.isEmpty()) {
                        Text("No stock movement recorded.", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
                    }
                    inventoryMovement.take(5).forEach { movement ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(movement.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.5f))
                            Text("${movement.sold}", color = Color.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                            Text("${if(movement.remaining == -1.0) "∞" else movement.remaining}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.weight(0.5f))
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. STAFF LEADERBOARD
            Text("Staff Performance Ranking", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            if (staffPerformance.isEmpty()) {
                Text("No staff sales recorded.", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
            }
            staffPerformance.take(3).forEach { perf ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = BusinessConfig.PrimaryColor, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(perf.email.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(perf.email.split("@")[0].uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${perf.saleCount} transactions completed", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        }
                        Text("${BusinessConfig.CURRENCY} ${perf.totalSales.toInt()}", color = BusinessConfig.SuccessColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 7. TRANSACTION AUDIT LOGS
            Text("Business Audit Logs", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            // EXPENSES LOG
            Text("Expense Audit Trail", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            filteredPurchases.reversed().take(10).forEach { p ->
                TransactionCard(p.title, "By: ${p.recordedBy}", p.totalAmount, p.status, p.auditReason) {
                    if(UserSession.hasPermission(UserPermissions.CAN_APPROVE)) purchaseViewModel.deletePurchase(p.id, "Admin Manual Cleanup")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // SALES LOG
            Text("Sales Audit Trail", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            filteredSales.reversed().take(10).forEach { s ->
                TransactionCard(s.customerName, "By: ${s.servedBy}", s.total, s.status, s.auditReason) {
                    if(UserSession.hasPermission(UserPermissions.CAN_APPROVE)) salesViewModel.deleteSale(s.id, "Admin Manual Cleanup")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 8. ADMINISTRATIVE CONTROLS
            if (UserSession.hasPermission(UserPermissions.CAN_APPROVE)) {
                Text("Administrative Sovereignty", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Root Management Tools", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Verify all transactions and manage historical data integrity.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { showApprovalPanel = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.FactCheck, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("APPROVE DATA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showWipeConfirm = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                            ) {
                                Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("WIPE RANGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 9. PROFESSIONAL EXPORTS (REINSTATED)
            Text("Reconciliation & Reporting", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            ExportButton("FULL EXCEL RECONCILIATION", Color(0xFF1E88E5)) {
                val range = "${Instant.fromEpochMilliseconds(fromDate).toLocalDateTime(TimeZone.currentSystemDefault()).date} to ${Instant.fromEpochMilliseconds(toDate).toLocalDateTime(TimeZone.currentSystemDefault()).date}"
                com.example.rshotsbilling.utils.PlatformReportUtils.exportExcel(filteredSales, filteredPurchases, range)
            }
            Spacer(modifier = Modifier.height(10.dp))
            ExportButton("PDF FINANCIAL SUMMARY", Color(0xFFD32F2F)) {
                com.example.rshotsbilling.utils.PlatformReportUtils.exportPdf(
                    filteredSales, filteredPurchases, summary.totalSales, summary.totalExpenses, summary.profit
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showFromPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = fromDate)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { fromDate = it }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("CANCEL") }
            }
        ) { DatePicker(state = state) }
    }

    if (showToPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = toDate)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { toDate = it }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("CANCEL") }
            }
        ) { DatePicker(state = state) }
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            containerColor = Color.White,
            title = { Text("Danger: Wipe Data?", fontWeight = FontWeight.Bold) },
            text = { Text("Permanently delete records in range? This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    reportsViewModel.wipeDataInRange(0, Long.MAX_VALUE)
                    showWipeConfirm = false
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("CONFIRM WIPE") }
            },
            dismissButton = { TextButton(onClick = { showWipeConfirm = false }) { Text("CANCEL") } }
        )
    }

    if (showApprovalPanel) {
        AlertDialog(
            onDismissRequest = { showApprovalPanel = false },
            containerColor = Color.White,
            title = { Text("Transaction Audit & Approval", fontWeight = FontWeight.Bold) },
            text = {
                val pSales = filteredSales.filter { it.status == "PENDING" }
                val pPurchases = filteredPurchases.filter { it.status == "PENDING" }
                if (pSales.isEmpty() && pPurchases.isEmpty()) {
                    Text("No pending transactions.", modifier = Modifier.padding(vertical = 20.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                        if (pSales.isNotEmpty()) {
                            item { Text("Sales to Verify", fontWeight = FontWeight.ExtraBold, color = BusinessConfig.PrimaryColor, fontSize = 12.sp) }
                            items(pSales) { s -> 
                                ApprovalRow(s.customerName, s.total, 
                                    onApprove = { auditItemToReview = Pair(s.id, "SALE"); isApproving = true; auditReason = "Approved" },
                                    onReject = { auditItemToReview = Pair(s.id, "SALE"); isApproving = false; auditReason = "" }
                                ) 
                            }
                        }
                        if (pPurchases.isNotEmpty()) {
                            item { Spacer(Modifier.height(16.dp)); Text("Expenses to Verify", fontWeight = FontWeight.ExtraBold, color = BusinessConfig.PrimaryColor, fontSize = 12.sp) }
                            items(pPurchases) { p -> 
                                ApprovalRow(p.title, p.totalAmount,
                                    onApprove = { auditItemToReview = Pair(p.id, "PURCHASE"); isApproving = true; auditReason = "Approved" },
                                    onReject = { auditItemToReview = Pair(p.id, "PURCHASE"); isApproving = false; auditReason = "" }
                                ) 
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showApprovalPanel = false }) { Text("CLOSE") } }
        )
    }

    if (auditItemToReview != null) {
        AlertDialog(
            onDismissRequest = { auditItemToReview = null },
            containerColor = Color.White,
            title = { Text(if (isApproving) "Confirm Approval" else "Reject Transaction", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a reason for this action:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = auditReason, onValueChange = { auditReason = it }, label = { Text("Reason / Comment") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (id, type) = auditItemToReview!!
                        if (type == "SALE") {
                            if (isApproving) salesViewModel.approveSale(id, auditReason)
                            else salesViewModel.rejectSale(id, auditReason)
                        } else {
                            if (isApproving) purchaseViewModel.approvePurchase(id, auditReason)
                            else purchaseViewModel.rejectPurchase(id, auditReason)
                        }
                        auditItemToReview = null
                        reportsViewModel.loadReportData(0, Long.MAX_VALUE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isApproving) BusinessConfig.SuccessColor else BusinessConfig.ErrorColor)
                ) { Text(if (isApproving) "APPROVE" else "REJECT") }
            },
            dismissButton = { TextButton(onClick = { auditItemToReview = null }) { Text("CANCEL") } }
        )
    }

    snackbarMessage?.let { msg ->
        AlertDialog(onDismissRequest = { snackbarMessage = null }, title = { Text("Info") }, text = { Text(msg) }, confirmButton = { Button(onClick = { snackbarMessage = null }) { Text("OK") } })
    }
}

@Composable
fun TransactionCard(name: String, subtitle: String, amount: Double, status: String, reason: String, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    if (reason.isNotEmpty() && status != "PENDING") {
                        Text("Audit: $reason", color = if(status == "APPROVED") Color.Cyan else Color.Yellow, fontSize = 9.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${BusinessConfig.CURRENCY} ${amount.toInt()}", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    if (status == "REJECTED" || status == "PENDING") {
                        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)) }
                    }
                }
            }
            if (status != "PENDING") {
                val stampColor = if (status == "APPROVED") BusinessConfig.SuccessColor else BusinessConfig.ErrorColor
                Surface(color = stampColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, stampColor.copy(alpha = 0.5f)), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 60.dp).rotate(-12f)) {
                    Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if(status == "APPROVED") Icons.Default.Verified else Icons.Default.Cancel, null, tint = stampColor, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(status, color = stampColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalRow(name: String, amount: Double, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f))) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${BusinessConfig.CURRENCY} $amount", fontSize = 12.sp)
            }
            Row {
                IconButton(onClick = onReject, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Cancel, null, tint = BusinessConfig.ErrorColor, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onApprove, modifier = Modifier.height(30.dp), colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor), contentPadding = PaddingValues(horizontal = 12.dp)) { Text("APPROVE", fontSize = 10.sp) }
            }
        }
    }
}

@Composable
fun DateCard(label: String, date: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(date, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ExportButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth(), 
        colors = ButtonDefaults.buttonColors(containerColor = color), 
        shape = RoundedCornerShape(8.dp)
    ) { 
        Text(text, fontWeight = FontWeight.Bold)
    }
}
