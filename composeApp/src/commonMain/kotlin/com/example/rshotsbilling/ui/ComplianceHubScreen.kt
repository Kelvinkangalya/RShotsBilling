package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.EtimsInvoice
import com.example.rshotsbilling.model.MpesaTransaction
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.viewmodel.EtimsViewModel
import com.example.rshotsbilling.viewmodel.MpesaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceHubScreen(
    etimsViewModel: EtimsViewModel,
    mpesaViewModel: MpesaViewModel,
    onBack: () -> Unit
) {
    if (!UserSession.hasPermission(UserPermissions.CAN_VIEW_COMPLIANCE)) {
        AccessDeniedScreen(onBack)
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("eTIMS Hub", "M-Pesa Hub")

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Compliance & Payments", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.White
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Row {
                                    Icon(if (index == 0) Icons.Default.CloudSync else Icons.Default.Smartphone, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(title)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> EtimsTab(etimsViewModel)
                1 -> MpesaTab(mpesaViewModel)
            }
        }
    }
}

@Composable
fun EtimsTab(viewModel: EtimsViewModel) {
    val totalInvoices by viewModel.totalInvoices.collectAsState()
    val totalTax by viewModel.totalTaxCollected.collectAsState()
    val pendingCount by viewModel.pendingInvoicesCount.collectAsState()
    val failedCount by viewModel.failedInvoicesCount.collectAsState()
    val submittedCount by viewModel.submittedInvoicesCount.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("eTIMS Dashboard", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            StatCard(Modifier.weight(1f), "Tax Collected", "${BusinessConfig.CURRENCY} ${"%.2f".format(totalTax)}", Icons.Default.AccountBalance, BusinessConfig.SuccessColor)
            StatCard(Modifier.weight(1f), "Total Invoices", totalInvoices.toString(), Icons.Default.Description, Color.Cyan)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            StatCard(Modifier.weight(1f), "Pending", pendingCount.toString(), Icons.Default.Pending, Color.Yellow)
            StatCard(Modifier.weight(1f), "Failed", failedCount.toString(), Icons.Default.Error, BusinessConfig.ErrorColor)
        }
        
        Spacer(Modifier.height(24.dp))
        Text("Recent e-Invoices", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(Modifier.height(8.dp))
        
        invoices.take(5).forEach { invoice ->
            EtimsInvoiceItem(invoice)
        }
    }
}

@Composable
fun MpesaTab(viewModel: MpesaViewModel) {
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val successCount by viewModel.successCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val failedCount by viewModel.failedCount.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("M-Pesa Dashboard", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            StatCard(Modifier.weight(1f), "Total Revenue", "${BusinessConfig.CURRENCY} ${"%.2f".format(totalRevenue)}", Icons.Default.Payments, BusinessConfig.SuccessColor)
            StatCard(Modifier.weight(1f), "Successful", successCount.toString(), Icons.Default.CheckCircle, Color.Cyan)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            StatCard(Modifier.weight(1f), "Pending", pendingCount.toString(), Icons.Default.Pending, Color.Yellow)
            StatCard(Modifier.weight(1f), "Failed", failedCount.toString(), Icons.Default.Error, BusinessConfig.ErrorColor)
        }
        
        Spacer(Modifier.height(24.dp))
        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(Modifier.height(8.dp))
        
        transactions.take(5).forEach { transaction ->
            MpesaTransactionItem(transaction)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color)
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun EtimsInvoiceItem(invoice: EtimsInvoice) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold)
                Text(invoice.customerName, fontSize = 11.sp, color = Color.Gray)
            }
            Text("${BusinessConfig.CURRENCY} ${invoice.totalAmount}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MpesaTransactionItem(transaction: MpesaTransaction) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Smartphone, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.phoneNumber, fontWeight = FontWeight.Bold)
                Text("Sale: ${transaction.saleId}", fontSize = 11.sp, color = Color.Gray)
            }
            Text("${BusinessConfig.CURRENCY} ${transaction.amount}", fontWeight = FontWeight.Bold)
        }
    }
}
