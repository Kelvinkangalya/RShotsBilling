package com.example.rshotsbilling

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.rshotsbilling.ui.*
import com.example.rshotsbilling.ui.payroll.*
import com.example.rshotsbilling.model.payroll.*
import com.example.rshotsbilling.model.Company
import com.example.rshotsbilling.model.StaffMember
import com.example.rshotsbilling.viewmodel.*
import com.example.rshotsbilling.utils.*
import com.example.rshotsbilling.ui.theme.RshotsBillingTheme
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {

    private val serviceViewModel: ServiceViewModel by viewModels()
    private val salesViewModel: SalesViewModel by viewModels()
    private val receiptViewModel: ReceiptViewModel by viewModels()
    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private val staffViewModel: StaffViewModel by viewModels()
    private val reportsViewModel: ReportsViewModel by viewModels()
    private val messagingViewModel: MessagingViewModel by viewModels()
    private val etimsViewModel: EtimsViewModel by viewModels()
    private val mpesaViewModel: MpesaViewModel by viewModels()
    private val payrollViewModel: PayrollViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize PDF Handlers for KMP UI
        com.example.rshotsbilling.utils.PlatformReceiptUtils.openPdfCallback = { sale ->
            val logo = com.example.rshotsbilling.utils.ReportPlatform.getLogoBytes()
            val bytes = com.example.rshotsbilling.utils.PdfReceiptGenerator.generateReceipt(logo, sale)
            if (bytes != null) handleGeneratedPdf(bytes, "Receipt_${sale.invoiceNumber}.pdf")
        }
        com.example.rshotsbilling.utils.PlatformReceiptUtils.sharePdfCallback = { sale ->
            val logo = com.example.rshotsbilling.utils.ReportPlatform.getLogoBytes()
            val bytes = com.example.rshotsbilling.utils.PdfReceiptGenerator.generateReceipt(logo, sale)
            if (bytes != null) handleGeneratedPdf(bytes, "Receipt_${sale.invoiceNumber}.pdf")
        }

        // Initialize Payroll Report Callbacks
        com.example.rshotsbilling.utils.PlatformReportUtils.exportPayslipCallback = { employee, payroll ->
            val bytes = com.example.rshotsbilling.utils.PdfGenerator.generatePayslip(employee, payroll)
            handleGeneratedPdf(bytes, "Payslip_${employee.fullName.replace(" ", "_")}.pdf")
        }
        com.example.rshotsbilling.utils.PlatformReportUtils.exportP9Callback = { employee, summary ->
            val bytes = com.example.rshotsbilling.utils.P9Generator.generateP9Pdf(employee, summary)
            handleGeneratedPdf(bytes, "P9_${employee.fullName.replace(" ", "_")}.pdf")
        }
        com.example.rshotsbilling.utils.PlatformReportUtils.exportPayrollExcelCallback = { employees, records, month, year ->
            val bytes = com.example.rshotsbilling.utils.ExcelReportExporter.exportPayrollReport(employees, records, month, year)
            handleGeneratedExcel(bytes, "Payroll_Register_${month}_${year}.xlsx")
        }
        com.example.rshotsbilling.utils.PlatformReportUtils.saveCsvCallback = { content, fileName ->
            val file = java.io.File(cacheDir, fileName)
            file.writeText(content)
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(intent, "Share CSV"))
        }
        com.example.rshotsbilling.utils.PlatformReportUtils.exportAllPayslipsCallback = { records, employees, month, year, asZip ->
            val bytes = com.example.rshotsbilling.utils.ExcelReportExporter.exportPayrollReport(employees, records, month, year)
            handleGeneratedExcel(bytes, "Payroll_Register_${month}_${year}.xlsx")
        }
        com.example.rshotsbilling.utils.PlatformReportUtils.exportPayrollRegisterPdfCallback = { records, employees, month, year ->
            val bytes = com.example.rshotsbilling.utils.PdfGenerator.generatePayrollRegister(records, employees, month, year)
            handleGeneratedPdf(bytes, "Payroll_Register_${month}_${year}.pdf")
        }

        com.example.rshotsbilling.worker.EtimsSyncWorker.scheduleSync(this)
        com.example.rshotsbilling.worker.MpesaSyncWorker.scheduleSync(this)

        setContent {
            RshotsBillingTheme {
                var currentScreen by remember { mutableStateOf("splash") }
                val scope = rememberCoroutineScope()
                
                // SIMPLE STARTUP FLOW
                LaunchedEffect(Unit) {
                    android.util.Log.d("STARTUP", "MainActivity: SplashScreen displayed, waiting 2s")
                    delay(2000)
                    android.util.Log.d("STARTUP", "MainActivity: Transitioning to login")
                    currentScreen = "login"
                }

                if (currentScreen == "splash") {
                    SplashScreen()
                } else {
                    var selectedServiceCategory by remember { mutableStateOf("") }
                    var selectedEmployeeId by remember { mutableStateOf<String?>(null) }
                    var selectedPayrollId by remember { mutableStateOf<String?>(null) }
                    var selectedP9Year by remember { mutableStateOf(2025) }
                    var selectedEmployeeForEdit by remember { mutableStateOf<EmployeeEntity?>(null) }

                    val refreshAllData = remember {
                        {
                            salesViewModel.loadSales()
                            receiptViewModel.loadReceipts()
                            purchaseViewModel.loadPurchases()
                        }
                    }

                    GradientBackground {
                        if (currentScreen != "login" && currentScreen != "dashboard") {
                            BackHandler {
                                currentScreen = when (currentScreen) {
                                    "add_service", "service_details" -> "services"
                                    "inventory_ledger", "batch_management", "procurement", "stock_adjustments", "expiry_alerts" -> "inventory_hub"
                                    "etims_invoices", "etims_settings", "mpesa_history", "mpesa_settings" -> "compliance_hub"
                                    else -> "dashboard"
                                }
                            }
                        }

                        when (currentScreen) {
                            "login" -> {
                                android.util.Log.d("STARTUP", "MainActivity: Rendering LoginScreen")
                                com.example.rshotsbilling.ui.LoginScreen(
                                    onLoginSuccess = { nextScreen: String ->
                                        android.util.Log.d("STARTUP", "MainActivity: Login success, moving to $nextScreen")
                                        refreshAllData()
                                        currentScreen = nextScreen
                                    }
                                )
                            }
                            
                            "loading_business" -> {
                                LaunchedEffect(Unit) {
                                    val businessId = UserSession.businessId
                                    val startTime = System.currentTimeMillis()
                                    android.util.Log.d("PERF", "loading_business state (Android) START. businessId: '$businessId'")
                                    
                                    if (businessId.isNotEmpty()) {
                                        try {
                                            val db = com.example.rshotsbilling.utils.FirestoreManager.db
                                            val queryStart = System.currentTimeMillis()
                                            
                                            val doc = withTimeoutOrNull(10000) {
                                                db.collection("companies").document(businessId).get()
                                            }
                                            android.util.Log.d("PERF", "Company profile query (Android) END: ${System.currentTimeMillis() - queryStart} ms")
                                            
                                            if (doc != null && doc.exists) {
                                                val company = doc.data(com.example.rshotsbilling.model.Company.serializer())
                                                UserSession.updateFromCompany(company)
                                                android.util.Log.d("RShotsBilling", "STEP 7: Company loaded: ${company.name}")
                                            } else {
                                                android.util.Log.d("RShotsBilling", "DEBUG: [LOGIN] Company doc DOES NOT EXIST for $businessId")
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.d("RShotsBilling", "DEBUG: [LOGIN] Business load FAILED: ${e.message}")
                                            e.printStackTrace()
                                        }
                                    } else {
                                        android.util.Log.d("RShotsBilling", "WARNING: [LOGIN] businessId is EMPTY in loading_business")
                                    }
                                    
                                    // Standard Navigation Logic
                                    val isSuperVendor = UserSession.userEmail?.equals(com.example.rshotsbilling.utils.BusinessConfig.SUPER_VENDOR_EMAIL, ignoreCase = true) == true
                                    
                                    val target = if (isSuperVendor) {
                                        "vendor_dashboard"
                                    } else if (UserSession.isActivationBypassEnabled || UserSession.isDeviceActivated) {
                                        "dashboard"
                                    } else {
                                        "activation"
                                    }
                                    android.util.Log.d("PERF", "loading_business state (Android) END: ${System.currentTimeMillis() - startTime} ms. Transitioning to $target")
                                    currentScreen = target
                                }
                                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
                                }
                            }

                            "dashboard" -> {
                                LaunchedEffect(Unit) {
                                    refreshAllData()
                                    serviceViewModel.loadServices()
                                    val email = UserSession.userEmail
                                    if (email != null) messagingViewModel.loadTasks(email)
                                }

                                DashboardScreen(
                                    salesViewModel = salesViewModel,
                                    purchaseViewModel = purchaseViewModel,
                                    serviceViewModel = serviceViewModel,
                                    staffViewModel = staffViewModel,
                                    messagingViewModel = messagingViewModel,
                                    onNavigate = { screen -> currentScreen = screen }
                                )
                            }

                            "sales_hub" -> {
                                LaunchedEffect(Unit) {
                                    serviceViewModel.loadServices()
                                    staffViewModel.loadStaff()
                                    receiptViewModel.loadReceipts()
                                    salesViewModel.loadSales()
                                }
                                val customerViewModel = remember { com.example.rshotsbilling.viewmodel.CustomerViewModel() }
                                SalesAndReceiptsHub(
                                    salesViewModel = salesViewModel,
                                    serviceViewModel = serviceViewModel,
                                    receiptViewModel = receiptViewModel,
                                    staffViewModel = staffViewModel,
                                    customerViewModel = customerViewModel,
                                    onBack = {
                                        refreshAllData()
                                        currentScreen = "dashboard"
                                    }
                                )
                            }

                            "inventory_hub" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                LaunchedEffect(Unit) { purchaseViewModel.loadPurchases() }
                                ProcurementAndInventoryHub(
                                    inventoryViewModel = invViewModel,
                                    purchaseViewModel = purchaseViewModel,
                                    onBack = {
                                        refreshAllData()
                                        currentScreen = "dashboard"
                                    },
                                    onNavigateInventory = { currentScreen = it }
                                )
                            }

                            "compliance_hub" -> {
                                ComplianceHubScreen(
                                    etimsViewModel = etimsViewModel,
                                    mpesaViewModel = mpesaViewModel,
                                    onBack = { currentScreen = "dashboard" }
                                )
                            }

                            "payroll_dashboard" -> {
                                HRPayrollScreen(
                                    viewModel = payrollViewModel,
                                    onBack = { currentScreen = "dashboard" }
                                )
                            }

                            "payslip_view" -> {
                                val employees by payrollViewModel.employees.collectAsState()
                                val records by payrollViewModel.currentMonthRecords.collectAsState()
                                val employee = employees.find { it.id == selectedEmployeeId }
                                val payroll = records.find { it.id == selectedPayrollId }
                                
                                PayslipScreen(
                                    employee = employee,
                                    payroll = payroll,
                                    onBack = { currentScreen = "payroll_dashboard" }
                                )
                            }

                            "p9_form_view" -> {
                                val employees by payrollViewModel.employees.collectAsState()
                                val allRecords by payrollViewModel.currentMonthRecords.collectAsState() 
                                val employee = employees.find { it.id == selectedEmployeeId }
                                
                                P9FormScreen(
                                    employee = employee,
                                    records = allRecords.filter { it.employeeId == selectedEmployeeId && it.year == selectedP9Year },
                                    year = selectedP9Year,
                                    onBack = { currentScreen = "employee_management" }
                                )
                            }

                            "services" -> {
                                LaunchedEffect(Unit) { serviceViewModel.loadServices() }
                                val customerViewModel = remember { com.example.rshotsbilling.viewmodel.CustomerViewModel() }
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
                                AddServiceScreen(
                                    viewModel = serviceViewModel,
                                    onBack = {
                                        serviceViewModel.loadServices()
                                        currentScreen = "services"
                                    }
                                )
                            }

                            "service_details" -> {
                                ServiceDetailsScreen(
                                    category = selectedServiceCategory,
                                    viewModel = serviceViewModel,
                                    onBack = { currentScreen = "services" }
                                )
                            }

                            "sales", "quick_sale" -> {
                                LaunchedEffect(Unit) {
                                    serviceViewModel.loadServices()
                                    staffViewModel.loadStaff()
                                }
                                val customerViewModel = remember { com.example.rshotsbilling.viewmodel.CustomerViewModel() }
                                SalesAndReceiptsHub(
                                    salesViewModel = salesViewModel,
                                    serviceViewModel = serviceViewModel,
                                    receiptViewModel = receiptViewModel,
                                    staffViewModel = staffViewModel,
                                    customerViewModel = customerViewModel,
                                    onBack = {
                                        refreshAllData()
                                        currentScreen = "dashboard"
                                    }
                                )
                            }

                            "receipts" -> {
                                LaunchedEffect(Unit) {
                                    receiptViewModel.loadReceipts()
                                    salesViewModel.loadSales()
                                }
                                ReceiptHistoryScreen(
                                    receiptViewModel = receiptViewModel,
                                    salesViewModel = salesViewModel,
                                    onBack = { currentScreen = "dashboard" }
                                )
                            }

                            "purchases" -> {
                                LaunchedEffect(Unit) { purchaseViewModel.loadPurchases() }
                                PurchasesScreen(
                                    viewModel = purchaseViewModel,
                                    onBack = {
                                        refreshAllData()
                                        currentScreen = "dashboard"
                                    }
                                )
                            }

                            "inventory_dashboard" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                InventoryDashboard(
                                    viewModel = invViewModel,
                                    onNavigate = { currentScreen = it }
                                )
                            }

                            "suppliers" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                SupplierScreen(viewModel = invViewModel)
                            }

                            "inventory_ledger" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                InventoryLedgerScreen(
                                    viewModel = invViewModel,
                                    onBack = { currentScreen = "inventory_dashboard" }
                                )
                            }

                            "batch_management" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                BatchManagementScreen(
                                    viewModel = invViewModel,
                                    onBack = { currentScreen = "inventory_dashboard" }
                                )
                            }

                            "procurement" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                ProcurementHubScreen(
                                    viewModel = invViewModel,
                                    onBack = { currentScreen = "inventory_dashboard" }
                                )
                            }

                            "stock_adjustments" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                StockAdjustmentScreen(
                                    viewModel = invViewModel,
                                    onBack = { currentScreen = "inventory_dashboard" }
                                )
                            }

                            "expiry_alerts" -> {
                                val invViewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                ExpiryAlertScreen(
                                    viewModel = invViewModel,
                                    onBack = { currentScreen = "inventory_dashboard" }
                                )
                            }

                            "inventory_reports" -> {
                                currentScreen = "reports"
                            }

                            "reports" -> {
                                LaunchedEffect(Unit) { refreshAllData() }
                                ReportsScreen(
                                    salesViewModel = salesViewModel,
                                    purchaseViewModel = purchaseViewModel,
                                    receiptViewModel = receiptViewModel,
                                    reportsViewModel = reportsViewModel,
                                    onBack = { currentScreen = "dashboard" }
                                )
                            }

                            "settings" -> {
                                SettingsScreen(
                                    staffViewModel = staffViewModel,
                                    reportsViewModel = reportsViewModel,
                                    messagingViewModel = messagingViewModel,
                                    salesViewModel = salesViewModel,
                                    purchaseViewModel = purchaseViewModel,
                                    onLogout = {
                                        UserSession.clear()
                                        currentScreen = "login"
                                    },
                                    onNavigate = { currentScreen = it },
                                    onBack = {
                                        currentScreen = "dashboard"
                                    }
                                )
                            }

                            "etims_dashboard", "etims_invoices", "etims_settings", "mpesa_dashboard", "mpesa_history", "mpesa_settings" -> {
                                currentScreen = "compliance_hub"
                            }

                            "task_center" -> {
                                TaskCenterScreen(
                                    messagingViewModel = messagingViewModel,
                                    staffViewModel = staffViewModel,
                                    onBack = { currentScreen = "dashboard" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    private fun handleGeneratedExcel(bytes: ByteArray, fileName: String) {
        try {
            val file = java.io.File(cacheDir, fileName)
            file.writeBytes(bytes)
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("RShotsBilling", "Error opening Excel: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun handleGeneratedPdf(bytes: ByteArray, fileName: String) {
        try {
            val file = java.io.File(cacheDir, fileName)
            file.writeBytes(bytes)
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("RShotsBilling", "Error opening PDF: ${e.message}")
            e.printStackTrace()
        }
    }
}

@Composable
fun SplashScreen() {
    GradientBackground {
        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text(
                    text = "RSHOTS CO LTD",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color.Black,
                    letterSpacing = 4.sp
                )
                Spacer(androidx.compose.ui.Modifier.height(8.dp))
                Text(
                    text = "Stability & Precision",
                    style = MaterialTheme.typography.labelLarge,
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}

