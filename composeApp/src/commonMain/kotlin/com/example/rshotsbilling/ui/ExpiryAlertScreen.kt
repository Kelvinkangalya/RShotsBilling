package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.InventoryBatchEntity
import com.example.rshotsbilling.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryAlertScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val expiringSoon by viewModel.expiringSoonProducts.collectAsState()
    val services by viewModel.services.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Expiry Alerts", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (expiringSoon.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products expiring soon", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(expiringSoon) { batch ->
                        val product = services.find { it.id == batch.productId }
                        val expDate = "N/A"
                        val isExpired = false

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isExpired) Color(0xFFFFEBEE) else Color.White)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(product?.name ?: "Unknown Product", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Batch: ${batch.batchNo}", fontSize = 12.sp, color = Color.Gray)
                                    Text("Expires: $expDate", color = if (isExpired) Color.Red else Color.Black, fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal)
                                }
                                Text("${batch.remainingQuantity} left", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
