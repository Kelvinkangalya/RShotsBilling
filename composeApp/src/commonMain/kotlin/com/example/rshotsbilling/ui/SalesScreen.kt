package com.example.rshotsbilling.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.Receipt
import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.SaleItem
import com.example.rshotsbilling.model.Service
import com.example.rshotsbilling.model.CustomerEntity
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.UserRole
import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.viewmodel.ReceiptViewModel
import com.example.rshotsbilling.viewmodel.SalesViewModel
import com.example.rshotsbilling.viewmodel.ServiceViewModel
import com.example.rshotsbilling.viewmodel.StaffViewModel
import com.example.rshotsbilling.viewmodel.CustomerViewModel
import com.example.rshotsbilling.ui.components.AddCustomerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    salesViewModel: SalesViewModel,
    serviceViewModel: ServiceViewModel,
    receiptViewModel: ReceiptViewModel,
    staffViewModel: StaffViewModel,
    customerViewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val services by serviceViewModel.services.collectAsState()
    val staffList by staffViewModel.staffList.collectAsState()
    val customers by customerViewModel.customers.collectAsState()

    var selectedService by remember { mutableStateOf<Service?>(null) }
    var quantity by remember { mutableStateOf("1") }
    
    // Customer Master Logic
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var customerKraPinInput by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    var servedBy by remember { mutableStateOf(UserSession.userEmail ?: "") }
    var paymentMethod by remember { mutableStateOf("M-Pesa") }
    var isEtims by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var customerSearchQuery by remember { mutableStateOf("") }

    val cartItems = remember { mutableStateListOf<SaleItem>() }

    var showMpesaManualDialog by remember { mutableStateOf(false) }
    var pendingSale by remember { mutableStateOf<Sale?>(null) }
    
    val categories = remember(services) { services.map { it.category }.distinct().filter { it.isNotEmpty() } }
    val filteredServices = services.filter { service ->
        (selectedCategory == null || service.category == selectedCategory) &&
        (service.name.contains(searchQuery, ignoreCase = true))
    }

    val filteredCustomers = remember(customers, customerSearchQuery) {
        if (customerSearchQuery.isEmpty()) customers.take(5)
        else customers.filter { 
            it.name.contains(customerSearchQuery, ignoreCase = true) || 
            it.phone.contains(customerSearchQuery) 
        }
    }

    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            initialCustomer = com.example.rshotsbilling.model.CustomerEntity(name = customerSearchQuery),
            onDismiss = { showAddCustomerDialog = false },
            onSave = { newCust ->
                customerViewModel.addCustomer(newCust) { success ->
                    if (success) {
                        selectedCustomer = newCust
                        customerNameInput = newCust.name
                        customerPhoneInput = newCust.phone
                        customerKraPinInput = newCust.kraPin
                        showAddCustomerDialog = false
                    }
                }
            }
        )
    }

    if (showMpesaManualDialog) {
        AlertDialog(
            onDismissRequest = { showMpesaManualDialog = false },
            containerColor = Color.White,
            title = { Text("M-Pesa Payment Required", fontWeight = FontWeight.Bold, color = BusinessConfig.ErrorColor) },
            text = { 
                Column {
                    Text("Please ask the customer to pay via Till Number:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TILL NUMBER", style = MaterialTheme.typography.labelMedium)
                            Text(BusinessConfig.TILL_NUMBER, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = BusinessConfig.SuccessColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("AMOUNT", style = MaterialTheme.typography.labelMedium)
                            Text("${BusinessConfig.CURRENCY} ${pendingSale?.total ?: 0.0}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Once the M-Pesa SMS is received, click below to finalize.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingSale?.let { sale ->
                            salesViewModel.addSale(sale)
                            receiptViewModel.addReceipt(com.example.rshotsbilling.model.Receipt(
                                saleId = sale.invoiceNumber, 
                                invoiceNumber = sale.invoiceNumber,
                                customerName = sale.customerName, 
                                customerPhone = sale.customerPhone,
                                customerKraPin = sale.customerKraPin, 
                                totalAmount = sale.total,
                                paymentMethod = sale.paymentMethod
                            ))
                        }
                        showMpesaManualDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor)
                ) { Text("CONFIRM TRANSACTION") }
            },
            dismissButton = { TextButton(onClick = { showMpesaManualDialog = false }) { Text("CANCEL") } }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Professional Billing Hub", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
            Text("Customer Information", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // CUSTOMER SELECTION UPGRADE
            var customerMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = customerMenuExpanded,
                onExpandedChange = { customerMenuExpanded = !customerMenuExpanded }
            ) {
                OutlinedTextField(
                    value = if (customerMenuExpanded) customerSearchQuery else (selectedCustomer?.name ?: customerNameInput),
                    onValueChange = { 
                        customerSearchQuery = it
                        customerNameInput = it
                        customerMenuExpanded = true
                        selectedCustomer = null // Reset selection if typing
                    },
                    label = { Text("Search or Enter Customer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(customerMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    placeholder = { Text("Type to find existing...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                ExposedDropdownMenu(
                    expanded = customerMenuExpanded,
                    onDismissRequest = { customerMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    filteredCustomers.forEach { cust ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(cust.name, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(cust.phone, fontSize = 12.sp, color = Color.Gray)
                                }
                            },
                            onClick = { 
                                selectedCustomer = cust
                                customerNameInput = cust.name
                                customerPhoneInput = cust.phone
                                customerKraPinInput = cust.kraPin
                                customerSearchQuery = ""
                                customerMenuExpanded = false 
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("ADD NEW CUSTOMER", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                            }
                        },
                        onClick = { 
                            showAddCustomerDialog = true
                            customerMenuExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customerPhoneInput, 
                onValueChange = { customerPhoneInput = it }, 
                label = { Text("Phone Number") }, 
                modifier = Modifier.fillMaxWidth(), 
                singleLine = true, 
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                readOnly = selectedCustomer != null
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customerKraPinInput, 
                onValueChange = { customerKraPinInput = it }, 
                label = { Text("KRA PIN (Optional)") }, 
                modifier = Modifier.fillMaxWidth(), 
                singleLine = true, 
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                readOnly = selectedCustomer != null
            )
            
            if (UserSession.userEmail?.equals("rauphinkelvin8@gmail.com", true) == true || UserSession.currentRole == UserRole.ADMIN) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Administrative Oversight", style = MaterialTheme.typography.titleMedium, color = Color.White)
                var staffMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = staffMenuExpanded, onExpandedChange = { staffMenuExpanded = !staffMenuExpanded }) {
                    OutlinedTextField(value = servedBy, onValueChange = {}, readOnly = true, label = { Text("Record As Served By") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(staffMenuExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    ExposedDropdownMenu(expanded = staffMenuExpanded, onDismissRequest = { staffMenuExpanded = false }) {
                        staffList.forEach { staff ->
                            DropdownMenuItem(text = { Text(staff.email) }, onClick = { servedBy = staff.email; staffMenuExpanded = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Receipt Type & Compliance", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Generate eTIMS Receipt", color = Color.White)
                Switch(
                    checked = isEtims,
                    onCheckedChange = { isEtims = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = BusinessConfig.SuccessColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Payment & Service", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            var paymentMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = paymentMenuExpanded, onExpandedChange = { paymentMenuExpanded = !paymentMenuExpanded }) {
                OutlinedTextField(value = paymentMethod, onValueChange = {}, readOnly = true, label = { Text("Payment Method") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(paymentMenuExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                ExposedDropdownMenu(expanded = paymentMenuExpanded, onDismissRequest = { paymentMenuExpanded = false }) {
                    listOf("M-Pesa", "Cash", "Bank Transfer").forEach { method ->
                        DropdownMenuItem(text = { Text(method) }, onClick = { paymentMethod = method; paymentMenuExpanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            var catMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = catMenuExpanded, onExpandedChange = { catMenuExpanded = !catMenuExpanded }) {
                OutlinedTextField(value = selectedCategory ?: "All Categories", onValueChange = {}, readOnly = true, label = { Text("Filter Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catMenuExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                ExposedDropdownMenu(
                    expanded = catMenuExpanded,
                    onDismissRequest = { catMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Categories", color = Color.Black) },
                        onClick = { selectedCategory = null; catMenuExpanded = false }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = Color.Black) },
                            onClick = { selectedCategory = cat; catMenuExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            var serviceMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = serviceMenuExpanded, onExpandedChange = { serviceMenuExpanded = !serviceMenuExpanded }) {
                OutlinedTextField(
                    value = if (serviceMenuExpanded) searchQuery else (selectedService?.name ?: ""),
                    onValueChange = { 
                        searchQuery = it
                        serviceMenuExpanded = true
                    },
                    label = { Text("Search Service") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(serviceMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    placeholder = { Text("Type to filter...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                ExposedDropdownMenu(
                    expanded = serviceMenuExpanded,
                    onDismissRequest = { serviceMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    filteredServices.forEach { service ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(service.name, fontWeight = FontWeight.Bold, color = Color.Black)
                                        if (service.stockQuantity <= BusinessConfig.LOW_STOCK_THRESHOLD && service.stockQuantity != -1.0) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(color = Color.Red, shape = CircleShape) {
                                                Text("${service.stockQuantity}", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 8.sp, color = Color.White)
                                            }
                                        }
                                    }
                                    Text("${BusinessConfig.CURRENCY} ${service.price}", fontSize = 12.sp, color = BusinessConfig.SuccessColor)
                                }
                            },
                            onClick = { 
                                selectedService = service
                                searchQuery = ""
                                serviceMenuExpanded = false 
                            }
                        )
                    }
                    if (filteredServices.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No services found", color = Color.Gray) },
                            onClick = { },
                            enabled = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val service = selectedService ?: return@Button
                    val qty = quantity.toDoubleOrNull() ?: 1.0
                    cartItems.add(SaleItem(serviceId = service.id, serviceName = service.name, quantity = qty, price = service.price))
                    selectedService = null
                    quantity = "1"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SecondaryColor),
                enabled = selectedService != null
            ) {
                Icon(Icons.Default.AddShoppingCart, null)
                Spacer(Modifier.width(8.dp))
                Text("ADD TO CART")
            }

            if (cartItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Current Cart (${cartItems.size} items)", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        cartItems.forEachIndexed { index, item ->
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.serviceName, color = Color.White, fontSize = 14.sp)
                                    Text("${item.quantity} x ${BusinessConfig.CURRENCY} ${item.price}", color = Color.LightGray, fontSize = 12.sp)
                                }
                                IconButton(onClick = { cartItems.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
                        val total = cartItems.sumOf { it.price * it.quantity }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Total Due:", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${BusinessConfig.CURRENCY} $total", color = BusinessConfig.SuccessColor, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (customerNameInput.isBlank() || cartItems.isEmpty()) {
                        return@Button
                    }
                    val timestamp = com.example.rshotsbilling.utils.getCurrentTimestamp()
                    val invoice = "${UserSession.invoicePrefix}$timestamp"
                    val total = cartItems.sumOf { it.price * it.quantity }
                    val sale = Sale(
                        invoiceNumber = invoice, 
                        customerName = customerNameInput, 
                        customerPhone = customerPhoneInput, 
                        customerKraPin = customerKraPinInput, 
                        total = total, 
                        paymentMethod = paymentMethod, 
                        servedBy = UserSession.displayName ?: servedBy,
                        isEtims = isEtims,
                        timestamp = timestamp
                    ).apply { items = cartItems.toList() }
                    
                    if (paymentMethod == "M-Pesa") {
                        pendingSale = sale
                        showMpesaManualDialog = true
                    } else {
                        salesViewModel.addSale(sale)
                        com.example.rshotsbilling.utils.PlatformReceiptUtils.openReceiptPdf(sale)
                        receiptViewModel.addReceipt(Receipt(
                            saleId = invoice, 
                            invoiceNumber = invoice, 
                            customerName = customerNameInput, 
                            customerPhone = customerPhoneInput, 
                            customerKraPin = customerKraPinInput, 
                            totalAmount = sale.total, 
                            paymentMethod = paymentMethod
                        ))
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor),
                enabled = cartItems.isNotEmpty()
            ) { Text("FINALIZE TRANSACTION", fontWeight = FontWeight.Bold) }
        }
    }
}
