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
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.Service
import com.example.rshotsbilling.model.InventoryBatchEntity
import com.example.rshotsbilling.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchManagementScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val services by viewModel.services.collectAsState()
    var selectedProduct by remember { mutableStateOf<Service?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Batch & Expiry", fontWeight = FontWeight.Bold, color = Color.White) },
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
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "Select Product",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Product", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    services.forEach { s ->
                        DropdownMenuItem(text = { Text(s.name) }, onClick = { selectedProduct = s; expanded = false })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (selectedProduct != null) {
                val batches by viewModel.getBatchesForProduct(selectedProduct!!.id).collectAsState(emptyList())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(batches) { batch ->
                        BatchItem(batch)
                    }
                }
            }
        }
    }
}

@Composable
fun BatchItem(batch: InventoryBatchEntity) {
    val expStr = "N/A"
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Batch: ${batch.batchNo}", fontWeight = FontWeight.Bold)
                Text("${batch.remainingQuantity} / ${batch.originalQuantity}", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expiry: $expStr", fontSize = 12.sp, color = Color.Gray)
                Text("Cost: ${batch.costPrice}", fontSize = 12.sp)
            }
        }
    }
}
