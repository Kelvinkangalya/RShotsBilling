package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.viewmodel.PurchaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    viewModel: PurchaseViewModel,
    onBack: () -> Unit
) {

    // =========================
    // FORM STATES
    // =========================
    var title by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var supplier by remember {
        mutableStateOf("")
    }

    var paymentMethod by remember {
        mutableStateOf("Cash")
    }

    var notes by remember {
        mutableStateOf("")
    }

    // =========================
    // PURCHASES LIST
    // =========================
    val purchases =
        viewModel.purchases.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.loadPurchases()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Inventory & Expenses", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),

            verticalArrangement = Arrangement.spacedBy(14.dp)

        ) {

            // =========================
            // FORM SECTION
            // =========================
            item {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = BusinessConfig.PrimaryColor)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Record New Expense",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // TITLE
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Item Name / Description") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // AMOUNT
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                label = { Text("Amount (${BusinessConfig.CURRENCY})") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // CATEGORY DROPDOWN
                            var categoryExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    BusinessConfig.EXPENSE_CATEGORIES.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                category = cat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // SUPPLIER
                            OutlinedTextField(
                                value = supplier,
                                onValueChange = { supplier = it },
                                label = { Text("Supplier") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // PAYMENT METHOD
                            var paymentExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = paymentExpanded,
                                onExpandedChange = { paymentExpanded = !paymentExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = paymentMethod,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Method") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = paymentExpanded,
                                    onDismissRequest = { paymentExpanded = false }
                                ) {
                                    listOf("Cash", "M-Pesa", "Bank Transfer", "Credit").forEach { method ->
                                        DropdownMenuItem(
                                            text = { Text(method) },
                                            onClick = {
                                                paymentMethod = method
                                                paymentExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // NOTES
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Additional Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // SAVE BUTTON
                        Button(
                            onClick = {
                                if (title.isBlank() || amount.isBlank() || category.isBlank()) return@Button
                                
                                val purchase = Purchase(
                                    title = title,
                                    itemName = title, 
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    totalAmount = amount.toDoubleOrNull() ?: 0.0,
                                    category = category,
                                    supplier = supplier,
                                    paymentMethod = paymentMethod,
                                    notes = notes,
                                    recordedBy = UserSession.userEmail ?: ""
                                )

                                viewModel.addPurchase(purchase)

                                // CLEAR
                                title = ""
                                amount = ""
                                category = ""
                                supplier = ""
                                paymentMethod = "Cash"
                                notes = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.PrimaryColor)
                        ) {
                            Text("SAVE TRANSACTION", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // =========================
            // PURCHASE LIST
            // =========================
            items(purchases.reversed()) { purchase ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = purchase.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = purchase.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (purchase.supplier.isNotEmpty()) {
                                Text(
                                    text = "From: ${purchase.supplier}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${BusinessConfig.CURRENCY} ${purchase.amount.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BusinessConfig.ErrorColor
                            )
                            Text(
                                text = purchase.paymentMethod,
                                style = MaterialTheme.typography.labelSmall,
                                color = BusinessConfig.PrimaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}