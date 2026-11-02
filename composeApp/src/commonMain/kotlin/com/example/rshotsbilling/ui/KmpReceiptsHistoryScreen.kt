package com.example.rshotsbilling.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.SaleItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.rshotsbilling.model.Receipt
import com.example.rshotsbilling.viewmodel.ReceiptViewModel
import com.example.rshotsbilling.viewmodel.SalesViewModel
import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.BusinessConfig

import com.example.rshotsbilling.utils.PrintingService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptHistoryScreen(
    receiptViewModel: ReceiptViewModel,
    salesViewModel: SalesViewModel? = null,
    onBack: () -> Unit
) {
    val receipts by receiptViewModel.receipts.collectAsState()
    val isLoading by receiptViewModel.isLoading.collectAsState()
    val sales by (salesViewModel?.sales?.collectAsState() ?: remember { mutableStateOf(emptyList<com.example.rshotsbilling.model.Sale>()) })
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedReceipt by remember { mutableStateOf<Receipt?>(null) }

    if (selectedReceipt != null) {
        ReceiptDetailsDialog(
            receipt = selectedReceipt!!,
            sale = sales?.find { it.invoiceNumber == selectedReceipt!!.invoiceNumber },
            onDismiss = { selectedReceipt = null }
        )
    }

    LaunchedEffect(Unit) {
        receiptViewModel.loadReceipts()
        salesViewModel?.loadSales()
    }

    val filteredReceipts = receipts.filter {
        it.customerName.contains(searchQuery, true) || it.invoiceNumber.contains(searchQuery, true)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Electronic Receipt History", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by customer or invoice #") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
            } else if (filteredReceipts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No records found.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredReceipts) { receipt ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = receipt.invoiceNumber, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        Text(text = receipt.customerName, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                    }
                                    Text(text = "${BusinessConfig.CURRENCY} ${receipt.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = BusinessConfig.SuccessColor, fontSize = 18.sp)
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Payment: ${receipt.paymentMethod}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    if (UserSession.hasPermission(UserPermissions.CAN_MANAGE_RECEIPTS)) {
                                        IconButton(onClick = { receiptViewModel.deleteReceipt(receipt.id) }) {
                                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f))
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            val sale = sales?.find { it.invoiceNumber == receipt.invoiceNumber }
                                            if (sale != null) {
                                                com.example.rshotsbilling.utils.PlatformReceiptUtils.shareReceiptPdf(sale)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, "Share", tint = Color.White.copy(alpha = 0.7f))
                                    }
                                    
                                    Button(
                                        onClick = { selectedReceipt = receipt },
                                        colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.PrimaryColor)
                                    ) {
                                        Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("VIEW", fontSize = 10.sp)
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Button(
                                        onClick = {
                                            val sale = sales?.find { it.invoiceNumber == receipt.invoiceNumber }
                                            if (sale != null) {
                                                com.example.rshotsbilling.utils.PlatformReceiptUtils.openReceiptPdf(sale)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("PDF", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDetailsDialog(
    receipt: Receipt,
    sale: Sale?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Receipt Details: ${receipt.invoiceNumber}", fontWeight = FontWeight.Bold, color = Color.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Customer: ${receipt.customerName}", color = Color.Black)
                Text("Date: ${com.example.rshotsbilling.utils.formatTimestamp(receipt.date)}", color = Color.Gray, fontSize = 12.sp)
                Text("Payment: ${receipt.paymentMethod}", color = Color.Black)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                if (sale != null) {
                    Text("Items:", fontWeight = FontWeight.Bold, color = Color.Black)
                    sale.items.forEach { item ->
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text(item.serviceName, modifier = Modifier.weight(1f), color = Color.Black)
                            Text("${item.quantity} x ${item.price}", color = Color.Black)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("TOTAL:", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Text("${BusinessConfig.CURRENCY} ${sale.total}", fontWeight = FontWeight.ExtraBold, color = BusinessConfig.SuccessColor)
                    }
                } else {
                    Text("Sale details not found.", color = Color.Red, fontSize = 12.sp)
                    Text("Total: ${BusinessConfig.CURRENCY} ${receipt.totalAmount}", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("CLOSE") }
        }
    )
}
