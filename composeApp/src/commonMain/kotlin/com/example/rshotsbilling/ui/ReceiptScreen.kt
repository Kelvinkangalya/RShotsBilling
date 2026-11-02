package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rshotsbilling.model.Receipt
import com.example.rshotsbilling.viewmodel.ReceiptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel,
    onBack: () -> Unit
) {
    val receipts by viewModel.receipts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadReceipts()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Receipt Repository", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (receipts.isEmpty()) {
                Text("No receipts found", color = Color.White.copy(alpha = 0.5f))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(receipts.reversed()) { receipt ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Invoice: ${receipt.invoiceNumber}", style = MaterialTheme.typography.titleMedium)
                            Text("Customer: ${receipt.customerName}")
                            Text("Total: KES ${receipt.totalAmount}")
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = {
                                    val sale = com.example.rshotsbilling.model.Sale(
                                        invoiceNumber = receipt.invoiceNumber,
                                        customerName = receipt.customerName,
                                        customerPhone = receipt.customerPhone,
                                        customerKraPin = receipt.customerKraPin,
                                        total = receipt.totalAmount,
                                        paymentMethod = receipt.paymentMethod,
                                        timestamp = receipt.date
                                    )
                                    com.example.rshotsbilling.utils.PlatformReceiptUtils.openReceiptPdf(sale)
                                }) {
                                    Text("OPEN")
                                }

                                OutlinedButton(onClick = {
                                    val sale = com.example.rshotsbilling.model.Sale(
                                        invoiceNumber = receipt.invoiceNumber,
                                        customerName = receipt.customerName,
                                        customerPhone = receipt.customerPhone,
                                        customerKraPin = receipt.customerKraPin,
                                        total = receipt.totalAmount,
                                        paymentMethod = receipt.paymentMethod,
                                        timestamp = receipt.date
                                    )
                                    com.example.rshotsbilling.utils.PlatformReceiptUtils.shareReceiptPdf(sale)
                                }) {
                                    Text("SHARE")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
