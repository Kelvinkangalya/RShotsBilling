package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.PurchaseOrderEntity
import com.example.rshotsbilling.model.PurchaseOrderItemEntity
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcurementHubScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val pos by viewModel.purchaseOrders.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Procurement Hub", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = BusinessConfig.PrimaryColor) {
                Icon(Icons.Default.Add, "Create PO", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (pos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No purchase orders found", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(pos) { po ->
                        POCard(po, onStatusUpdate = { viewModel.updatePOStatus(po.id, it) })
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePODialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
            onConfirm = { po, items ->
                viewModel.createPurchaseOrder(po, items)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun POCard(po: PurchaseOrderEntity, onStatusUpdate: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PO #: ${po.id.take(8).uppercase()}", fontWeight = FontWeight.Bold)
                POStatusBadge(po.status)
            }
            Spacer(Modifier.height(8.dp))
            Text("Total: ${BusinessConfig.CURRENCY} ${po.totalAmount}", fontWeight = FontWeight.ExtraBold)
            
            if (po.status == "PENDING" || po.status == "APPROVED") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onStatusUpdate("CANCELLED") }) { Text("CANCEL", color = Color.Red) }
                    if (po.status == "PENDING") {
                        Button(onClick = { onStatusUpdate("APPROVED") }) { Text("APPROVE") }
                    } else if (po.status == "APPROVED") {
                        Button(onClick = { onStatusUpdate("COMPLETED") }) { Text("ORDER") }
                    }
                }
            }
        }
    }
}

@Composable
fun POStatusBadge(status: String) {
    val color = when(status) {
        "PENDING" -> Color.Gray
        "APPROVED" -> Color.Blue
        "COMPLETED" -> Color(0xFF4CAF50)
        "CANCELLED" -> Color.Red
        else -> Color.DarkGray
    }
    Surface(color = color.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
        Text(status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CreatePODialog(viewModel: InventoryViewModel, onDismiss: () -> Unit, onConfirm: (PurchaseOrderEntity, List<PurchaseOrderItemEntity>) -> Unit) {
    val suppliers by viewModel.suppliers.collectAsState()
    var selectedSupplierId by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("0") }
    var cost by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Purchase Request") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Supplier", fontWeight = FontWeight.Bold)
                suppliers.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedSupplierId == s.id, onClick = { selectedSupplierId = s.id })
                        Text(s.name)
                    }
                }
                
                HorizontalDivider()
                Text("Item Details", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Unit Cost") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val poId = "po_0"
                val po = PurchaseOrderEntity(
                    id = poId,
                    supplierId = selectedSupplierId,
                    totalAmount = (qty.toDoubleOrNull() ?: 0.0) * (cost.toDoubleOrNull() ?: 0.0),
                    status = "PENDING"
                )
                val items = listOf(PurchaseOrderItemEntity(id = "poi_0", poId = poId, productId = "test", productName = "Standard PO Item", quantity = qty.toDoubleOrNull() ?: 0.0, unitCost = cost.toDoubleOrNull() ?: 0.0))
                onConfirm(po, items)
            }) { Text("Submit Request") }
        }
    )
}
