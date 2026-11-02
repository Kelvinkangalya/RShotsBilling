package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.SupplierEntity
import com.example.rshotsbilling.viewmodel.InventoryViewModel

@Composable
fun SupplierScreen(viewModel: InventoryViewModel) {
    val suppliers by viewModel.suppliers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, "Add Supplier")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Supplier Network", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(16.dp))

            if (suppliers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No suppliers registered", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(suppliers) { supplier ->
                        SupplierCard(supplier)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, contact, phone, kra ->
                viewModel.addSupplier(SupplierEntity(id = "sup_0", name = name, contactPerson = contact, phone = phone, kraPin = kra))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SupplierCard(supplier: SupplierEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(supplier.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Contact: ${supplier.contactPerson}", fontSize = 14.sp)
                Text("Phone: ${supplier.phone}", fontSize = 14.sp)
                Text("KRA PIN: ${supplier.kraPin}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AddSupplierDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var kra by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") })
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Person") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
                OutlinedTextField(value = kra, onValueChange = { kra = it }, label = { Text("KRA PIN") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, contact, phone, kra) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
