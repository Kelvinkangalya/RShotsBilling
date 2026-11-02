package com.example.rshotsbilling

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.ui.*
import com.example.rshotsbilling.ui.theme.RshotsBillingTheme
import com.example.rshotsbilling.viewmodel.*
import com.example.rshotsbilling.utils.*
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.ui.graphics.vector.ImageVector

import com.example.rshotsbilling.utils.*
import androidx.compose.runtime.collectAsState

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun App() {
    KMPLogger.log("DEBUG: [App] Composable body execution.")
    
    val firebaseState by FirebaseStatus.state.collectAsState()
    
    RshotsBillingTheme {
        var currentScreen by remember { mutableStateOf("splash") }
        var selectedServiceCategory by remember { mutableStateOf("") }
        
        // UNIFIED STARTUP FLOW
        LaunchedEffect(firebaseState) {
            val startTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            KMPLogger.log("[STARTUP] App Startup. FirebaseState: $firebaseState, currentScreen: $currentScreen")
            
            if (currentScreen == "splash" && firebaseState == FirebaseState.Ready) {
                try {
                    val viewModel = LoginViewModel()
                    KMPLogger.log("[STARTUP] Checking for existing session START")
                    val sessionStart = getCurrentTimestamp()
                    
                    val target = withTimeoutOrNull(5000) {
                        viewModel.restoreSession()
                    } ?: "login"
                    
                    KMPLogger.log("[STARTUP] Checking for existing session END: ${getCurrentTimestamp() - sessionStart} ms. Target: $target")
                    currentScreen = target
                } catch (e: Exception) {
                    KMPLogger.log("[STARTUP] Startup logic FAILED: ${e.message}")
                    currentScreen = "login"
                }
            } else if (firebaseState == FirebaseState.Error) {
                KMPLogger.log("[STARTUP] Firebase initialization FAILED. Falling back to Login Screen.")
                currentScreen = "login"
            }
        }

        if (currentScreen == "splash") {
            SplashScreen()
        } else {
            // Main Navigation
            GradientBackground {
                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginSuccess = { nextScreen -> 
                                KMPLogger.log("DEBUG: [Navigation] Login success, moving to $nextScreen")
                                currentScreen = nextScreen
                            }
                        )
                    }
                    "password_change" -> {
                        PasswordChangeScreen(
                            onSuccess = { 
                                currentScreen = if (UserSession.currentRole == UserRole.ADMIN || UserSession.currentRole == UserRole.CEO) "company_setup" else "dashboard"
                            }
                        )
                    }
                    "company_setup" -> {
                        CompanySetupWizard(
                            onComplete = { currentScreen = "dashboard" }
                        )
                    }
                    "loading_business" -> {
                        LaunchedEffect(Unit) {
                            val businessId = UserSession.businessId
                            val startTime = getCurrentTimestamp()
                            KMPLogger.log("[PERF] loading_business state START. businessId: '$businessId'")
                            
                            // FAIL-SAFE: Re-fetch staff profile if businessId is missing
                            if (businessId.isEmpty() && UserSession.userEmail != null) {
                                try {
                                    val db = FirestoreManager.db
                                    KMPLogger.log("[PERF] Staff profile recovery query START for ${UserSession.userEmail}")
                                    val queryStart = getCurrentTimestamp()
                                    val emailQuery = withTimeoutOrNull(5000) {
                                        db.collection("staff").where { "email" equalTo UserSession.userEmail!! }.get()
                                    }
                                    KMPLogger.log("[PERF] Staff profile recovery query END: ${getCurrentTimestamp() - queryStart} ms")

                                    if (emailQuery != null && emailQuery.documents.isNotEmpty()) {
                                        val staff = emailQuery.documents.first().data(com.example.rshotsbilling.model.StaffMember.serializer())
                                        UserSession.updateFromStaff(staff)
                                        KMPLogger.log("DEBUG: [LOGIN] Staff profile recovered. businessId: ${UserSession.businessId}")
                                    }
                                } catch (e: Exception) {
                                    KMPLogger.log("DEBUG: [LOGIN] Staff recovery failed: ${e.message}")
                                }
                            }

                            val finalBusinessId = UserSession.businessId
                            if (finalBusinessId.isNotEmpty()) {
                                try {
                                    val db = FirestoreManager.db
                                    KMPLogger.log("[PERF] Company profile query START: companies/$finalBusinessId")
                                    val queryStart = getCurrentTimestamp()
                                    val doc = withTimeoutOrNull(5000) {
                                        db.collection("companies").document(finalBusinessId).get()
                                    }
                                    KMPLogger.log("[PERF] Company profile query END: ${getCurrentTimestamp() - queryStart} ms")
                                    
                                    if (doc != null && doc.exists) {
                                        val company = doc.data(com.example.rshotsbilling.model.Company.serializer())
                                        UserSession.updateFromCompany(company)
                                        KMPLogger.log("STEP 7: Company loaded: ${company.name}")
                                    } else {
                                        KMPLogger.log("DEBUG: [LOGIN] Company profile fetch TIMED OUT or NOT FOUND.")
                                        UserSession.isSessionReady = true // Failsafe
                                    }
                                } catch (e: Exception) {
                                    KMPLogger.log("DEBUG: [LOGIN] Business load failed: ${e.message}")
                                    UserSession.isSessionReady = true // Failsafe
                                }
                            } else {
                                KMPLogger.log("WARNING: [LOGIN] loading_business reached but businessId is still empty.")
                                UserSession.isSessionReady = true // Failsafe
                            }
                            
                            val isSuperVendor = UserSession.userEmail?.equals(BusinessConfig.SUPER_VENDOR_EMAIL, ignoreCase = true) == true
                            
                            val target = if (isSuperVendor) {
                                "vendor_dashboard"
                            } else if (UserSession.isActivationBypassEnabled || UserSession.isDeviceActivated) {
                                "dashboard"
                            } else {
                                "activation"
                            }
                            
                            KMPLogger.log("[PERF] loading_business state END: ${getCurrentTimestamp() - startTime} ms. Transitioning to $target")
                            currentScreen = target
                        }
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(Modifier.height(24.dp))
                                Text("Synchronizing Business Config...", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    "activation" -> {
                        ActivationScreen(
                            onActivated = { currentScreen = "dashboard" },
                            onLogout = { currentScreen = "login" }
                        )
                    }
                    "dashboard" -> {
                        KMPLogger.log("STEP 9: Dashboard displayed.")
                        val salesViewModel = remember { SalesViewModel() }
                        val purchaseViewModel = remember { PurchaseViewModel() }
                        val serviceViewModel = remember { ServiceViewModel() }
                        val staffViewModel = remember { StaffViewModel() }
                        val messagingViewModel = remember { MessagingViewModel() }
                        val inventoryViewModel = remember { InventoryViewModel() }
                        val customerViewModel = remember { CustomerViewModel() }

                        LaunchedEffect(Unit) {
                            KMPLogger.log("DEBUG: [Navigation] Dashboard reached, triggering lazy data loads.")
                            salesViewModel.loadSales()
                            purchaseViewModel.loadPurchases()
                            serviceViewModel.loadServices()
                            staffViewModel.loadStaff()
                            messagingViewModel.loadTasks(com.example.rshotsbilling.utils.UserSession.userEmail ?: "")
                            inventoryViewModel.loadData()
                            
                            // AUTO-MIGRATION CHECK
                            if (UserSession.currentRole == UserRole.ADMIN || UserSession.currentRole == UserRole.CEO) {
                                customerViewModel.triggerMigration()
                            }
                        }

                        DashboardScreen(
                            salesViewModel = salesViewModel,
                            purchaseViewModel = purchaseViewModel,
                            serviceViewModel = serviceViewModel,
                            staffViewModel = staffViewModel,
                            messagingViewModel = messagingViewModel,
                            onNavigate = { screen -> 
                                KMPLogger.log("DEBUG: [Navigation] Moving to $screen")
                                currentScreen = screen 
                            }
                        )
                    }
                    "sales", "quick_sale" -> {
                        val salesViewModel = remember { SalesViewModel() }
                        val serviceViewModel = remember { ServiceViewModel() }
                        val receiptViewModel = remember { ReceiptViewModel() }
                        val staffViewModel = remember { StaffViewModel() }
                        val customerViewModel = remember { CustomerViewModel() }
                        
                        LaunchedEffect(Unit) {
                            salesViewModel.loadSales()
                            serviceViewModel.loadServices()
                            receiptViewModel.loadReceipts()
                            staffViewModel.loadStaff()
                        }

                        SalesAndReceiptsHub(
                            salesViewModel = salesViewModel,
                            serviceViewModel = serviceViewModel,
                            receiptViewModel = receiptViewModel,
                            staffViewModel = staffViewModel,
                            customerViewModel = customerViewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    "sales_hub" -> {
                        val salesViewModel = remember { SalesViewModel() }
                        val serviceViewModel = remember { ServiceViewModel() }
                        val receiptViewModel = remember { ReceiptViewModel() }
                        val staffViewModel = remember { StaffViewModel() }
                        val customerViewModel = remember { CustomerViewModel() }
                        
                        LaunchedEffect(Unit) {
                            salesViewModel.loadSales()
                            serviceViewModel.loadServices()
                            receiptViewModel.loadReceipts()
                            staffViewModel.loadStaff()
                        }

                        SalesAndReceiptsHub(
                            salesViewModel = salesViewModel,
                            serviceViewModel = serviceViewModel,
                            receiptViewModel = receiptViewModel,
                            staffViewModel = staffViewModel,
                            customerViewModel = customerViewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    "services" -> {
                        val serviceViewModel = remember { ServiceViewModel() }
                        val customerViewModel = remember { CustomerViewModel() }
                        LaunchedEffect(Unit) { serviceViewModel.loadServices() }
                        ServicesScreen(
                            viewModel = serviceViewModel,
                            customerViewModel = customerViewModel,
                            onBack = { currentScreen = "dashboard" },
                            onSelectCategory = { category ->
                                if (category == "ADD_SERVICE") {
                                    currentScreen = "add_service"
                                } else {
                                    selectedServiceCategory = category
                                    currentScreen = "service_details"
                                }
                            }
                        )
                    }
                    "add_service" -> {
                        val serviceViewModel = remember { ServiceViewModel() }
                        AddServiceScreen(
                            viewModel = serviceViewModel,
                            onBack = { currentScreen = "services" }
                        )
                    }
                    "service_details" -> {
                        val serviceViewModel = remember { ServiceViewModel() }
                        ServiceDetailsScreen(
                            category = selectedServiceCategory,
                            viewModel = serviceViewModel,
                            onBack = { currentScreen = "services" }
                        )
                    }
                    "inventory_hub" -> {
                        val inventoryViewModel = remember { InventoryViewModel() }
                        val purchaseViewModel = remember { PurchaseViewModel() }
                        LaunchedEffect(Unit) {
                            inventoryViewModel.loadData()
                            purchaseViewModel.loadPurchases()
                        }
                        ProcurementAndInventoryHub(
                            inventoryViewModel = inventoryViewModel,
                            purchaseViewModel = purchaseViewModel,
                            onBack = { currentScreen = "dashboard" },
                            onNavigateInventory = { currentScreen = it }
                        )
                    }
                    "reports" -> {
                        val salesViewModel = remember { SalesViewModel() }
                        val purchaseViewModel = remember { PurchaseViewModel() }
                        val receiptViewModel = remember { ReceiptViewModel() }
                        val reportsViewModel = remember { ReportsViewModel() }
                        
                        LaunchedEffect(Unit) {
                            salesViewModel.loadSales()
                            purchaseViewModel.loadPurchases()
                            receiptViewModel.loadReceipts()
                        }

                        KmpReportsScreen(
                            salesViewModel = salesViewModel,
                            purchaseViewModel = purchaseViewModel,
                            receiptViewModel = receiptViewModel,
                            reportsViewModel = reportsViewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    "payroll_dashboard" -> {
                        val payrollViewModel = remember { PayrollViewModel() }
                        LaunchedEffect(Unit) {
                            payrollViewModel.loadEmployees()
                            val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            payrollViewModel.loadPayrollRecords(now.monthNumber, now.year)
                        }
                        HRPayrollScreen(
                            viewModel = payrollViewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    "compliance_hub" -> {
                        val etimsViewModel = remember { EtimsViewModel() }
                        val mpesaViewModel = remember { MpesaViewModel() }
                        LaunchedEffect(Unit) {
                            etimsViewModel.loadInvoices()
                            mpesaViewModel.loadTransactions()
                        }
                        ComplianceHubScreen(
                            etimsViewModel = etimsViewModel,
                            mpesaViewModel = mpesaViewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    "task_center" -> {
                        val messagingViewModel = remember { MessagingViewModel() }
                        val staffViewModel = remember { StaffViewModel() }
                        LaunchedEffect(Unit) {
                            messagingViewModel.loadTasks(com.example.rshotsbilling.utils.UserSession.userEmail ?: "")
                            staffViewModel.loadStaff()
                        }
                        TaskCenterScreen(
                            messagingViewModel = messagingViewModel,
                            staffViewModel = staffViewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    "settings" -> {
                        val staffViewModel = remember { StaffViewModel() }
                        val reportsViewModel = remember { ReportsViewModel() }
                        val messagingViewModel = remember { MessagingViewModel() }
                        val salesViewModel = remember { SalesViewModel() }
                        val purchaseViewModel = remember { PurchaseViewModel() }
                        SettingsScreen(
                            staffViewModel = staffViewModel,
                            reportsViewModel = reportsViewModel,
                            messagingViewModel = messagingViewModel,
                            salesViewModel = salesViewModel,
                            purchaseViewModel = purchaseViewModel,
                            onLogout = { 
                                com.example.rshotsbilling.utils.UserSession.clear()
                                currentScreen = "login" 
                            },
                            onNavigate = { currentScreen = it },
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                    "vendor_dashboard" -> {
                        VendorAdminDashboard(onBack = { currentScreen = "dashboard" })
                    }
                    "business_admin_dashboard" -> {
                        BusinessAdminDashboard(onBack = { currentScreen = "dashboard" })
                    }
                    "device_diagnostics" -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Diagnostics disabled during stabilization.", color = Color.White)
                        }
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Screen '$currentScreen' not yet implemented in KMP")
                                Button(onClick = { currentScreen = "dashboard" }) { Text("Back to Dashboard") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    GradientBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = BusinessConfig.COMPANY_NAME.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    letterSpacing = 4.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = UserSession.companyMotto.ifEmpty { "Stability & Precision" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StartupErrorScreen(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("âš ï¸", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            Text("Connection Error", style = MaterialTheme.typography.headlineSmall)
            Text(
                "System could not initialize. Please check your internet connection and try again.",
                color = MaterialTheme.colorScheme.error,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("RETRY")
            }
        }
    }
}

