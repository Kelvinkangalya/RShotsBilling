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
import com.example.rshotsbilling.model.Service
import com.example.rshotsbilling.model.StockMovementEntity
import com.example.rshotsbilling.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryLedgerScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val services by viewModel.services.collectAsState()
    var selectedProduct by remember { mutableStateOf<Service?>(null) }
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stock Ledger", fontWeight = FontWeight.Bold, color = Color.White) },
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
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "Select Product",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by Product", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    services.forEach { service ->
                        DropdownMenuItem(
                            text = { Text(service.name) },
                            onClick = {
                                selectedProduct = service
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (selectedProduct != null) {
                val movements by viewModel.getMovementsForProduct(selectedProduct!!.id).collectAsState(emptyList())
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(movements) { movement ->
                        MovementItem(movement)
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a product to view history", color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun MovementItem(movement: StockMovementEntity) {
    val dateStr = "N/A"
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(movement.type, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(movement.reason, fontSize = 12.sp, color = Color.Gray)
                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
            }
            Text(
                text = (if (movement.quantity > 0) "+" else "") + movement.quantity.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if (movement.quantity > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}
