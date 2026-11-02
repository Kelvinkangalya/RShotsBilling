package com.example.rshotsbilling.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.CustomerEntity
import com.example.rshotsbilling.viewmodel.CustomerViewModel
import com.example.rshotsbilling.viewmodel.ServiceViewModel
import com.example.rshotsbilling.ui.components.AddCustomerDialog
import com.example.rshotsbilling.utils.BusinessConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: ServiceViewModel,
    customerViewModel: CustomerViewModel,
    onBack: () -> Unit,
    onSelectCategory: (String) -> Unit
) {
    val services by viewModel.services.collectAsState()
    val customers by customerViewModel.customers.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Services, 1: Customers

    // Dynamic categories from data + defaults
    val categories = remember(services) {
        val base = listOf("Photography", "Cyber Services", "Tax Consultancy")
        val fromData = services.map { it.category }.filter { it.isNotEmpty() }
        (base + fromData).distinct()
    }

    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Services & Customers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("SERVICES", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("CUSTOMERS", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                ServiceCategoryList(categories, onSelectCategory)
            } else {
                var showAddCustomerDialog by remember { mutableStateOf(false) }
                
                if (showAddCustomerDialog) {
                    AddCustomerDialog(
                        initialCustomer = null,
                        onDismiss = { showAddCustomerDialog = false },
                        onSave = { newCust ->
                            customerViewModel.addCustomer(newCust) { success ->
                                if (success) {
                                    showAddCustomerDialog = false
                                    customerViewModel.loadCustomers()
                                }
                            }
                        }
                    )
                }

                CustomerList(
                    customers = customers, 
                    viewModel = customerViewModel,
                    onAddCustomer = { showAddCustomerDialog = true }
                )
            }
        }
    }
}

@Composable
fun ServiceCategoryList(categories: List<String>, onSelectCategory: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Button(
                onClick = { onSelectCategory("ADD_SERVICE") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SecondaryColor)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("ADD NEW SERVICE", fontWeight = FontWeight.Bold)
            }
        }
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectCategory(category) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerList(
    customers: List<CustomerEntity>, 
    viewModel: CustomerViewModel,
    onAddCustomer: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCustomers = remember(customers, searchQuery) {
        viewModel.searchCustomers(searchQuery)
    }
    
    val focusManager = LocalFocusManager.current
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAddCustomer,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("ADD NEW CUSTOMER", fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by name, phone or email...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredCustomers) { customer ->
                CustomerCard(customer, onEdit = { editingCustomer = customer })
            }
            if (filteredCustomers.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No customers found.", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (editingCustomer != null) {
        AddCustomerDialog(
            initialCustomer = editingCustomer,
            onDismiss = { editingCustomer = null },
            onSave = { updatedCust ->
                viewModel.addCustomer(updatedCust) { success ->
                    if (success) {
                        editingCustomer = null
                    }
                }
            }
        )
    }
}

@Composable
fun CustomerCard(customer: CustomerEntity, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(customer.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit Customer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Text("Phone: ${customer.phone}", fontSize = 14.sp)
            if (customer.email.isNotEmpty()) {
                Text("Email: ${customer.email}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Visits: ${customer.numberOfPurchases}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text("Total: KES ${customer.totalSpending}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
