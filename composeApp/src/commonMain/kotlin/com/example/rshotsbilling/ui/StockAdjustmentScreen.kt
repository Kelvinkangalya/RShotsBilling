package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rshotsbilling.model.Service
import com.example.rshotsbilling.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val services by viewModel.services.collectAsState()
    var selectedProduct by remember { mutableStateOf<Service?>(null) }
    var adjustmentQty by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("ADJUSTMENT") } 

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stock Adjustments", fontWeight = FontWeight.Bold, color = Color.White) },
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Manual Adjustment Form", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            services.forEach { s ->
                                DropdownMenuItem(text = { Text(s.name) }, onClick = { selectedProduct = s; expanded = false })
                            }
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = adjustmentQty,
                            onValueChange = { adjustmentQty = it },
                            label = { Text("Qty Change (+/-)") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        var typeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = type,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reason Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                listOf("ADJUSTMENT", "DAMAGED", "LOST", "EXPIRED", "RETURN_OUT").forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Note / Explanation") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (selectedProduct != null && adjustmentQty.isNotEmpty()) {
                                viewModel.adjustStock(selectedProduct!!.id, adjustmentQty.toDoubleOrNull() ?: 0.0, type, reason)
                                adjustmentQty = ""
                                reason = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("POST ADJUSTMENT")
                    }
                }
            }
        }
    }
}
