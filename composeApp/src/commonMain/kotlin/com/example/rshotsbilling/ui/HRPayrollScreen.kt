package com.example.rshotsbilling.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.rshotsbilling.model.payroll.*
import com.example.rshotsbilling.utils.*
import com.example.rshotsbilling.viewmodel.PayrollViewModel
import com.example.rshotsbilling.viewmodel.BulkProcessSummary
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRPayrollScreen(
    viewModel: PayrollViewModel,
    onBack: () -> Unit
) {
    if (!UserSession.hasPermission(UserPermissions.CAN_MANAGE_PAYROLL)) {
        AccessDeniedScreen(onBack)
        return
    }

    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Calculation", "Payslips", "P9 Forms", "CSV / P10", "Settings", "Employees")
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Human Resources & Payroll", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                when (selectedTab) {
                    0 -> PayrollCalculationPanel(viewModel)
                    1 -> PayslipsPanel(viewModel)
                    2 -> P9Panel(viewModel)
                    3 -> P10CsvPanel(viewModel)
                    4 -> PayrollSettingsPanel(viewModel)
                    5 -> EmployeeManagementPanel(viewModel)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- PANEL A: PAYROLL CALCULATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollCalculationPanel(viewModel: PayrollViewModel) {
    val employees by viewModel.employees.collectAsState()
    val processedResult by viewModel.processedResult.collectAsState()
    val bulkSummary by viewModel.bulkSummary.collectAsState()
    
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var month by remember { mutableStateOf(now.monthNumber) }
    var year by remember { mutableStateOf(now.year) }

    var processingMode by remember { mutableStateOf(1) } // 0: Bulk, 1: Individual
    var searchQuery by remember { mutableStateOf("") }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadEmployees()
    }

    GlassPanel(title = "Panel A: Payroll Calculation", icon = Icons.Default.Calculate) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Mode Selection
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = processingMode == 1,
                    onClick = { processingMode = 1; viewModel.clearResults() },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Individual") }
                SegmentedButton(
                    selected = processingMode == 0,
                    onClick = { processingMode = 0; viewModel.clearResults() },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Process All") }
            }

            // Month/Year Selection
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = month.toString(),
                    onValueChange = { month = it.toIntOrNull() ?: month },
                    label = { Text("Month (1-12)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { year = it.toIntOrNull() ?: year },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            if (processingMode == 1) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Select Employee", color = Color.White) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    val filtered = employees.filter { it.fullName.contains(searchQuery, ignoreCase = true) }
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        filtered.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text("${emp.fullName} (${emp.employeeNumber})") },
                                onClick = {
                                    selectedEmployeeId = emp.id
                                    searchQuery = emp.fullName
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { 
                        if (selectedEmployeeId.isNotEmpty()) {
                            viewModel.processPayrollForEmployee(selectedEmployeeId, month, year)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedEmployeeId.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("CALCULATE / PROCESS")
                }

                AnimatedVisibility(processedResult != null) {
                    processedResult?.let { record ->
                        CalculationResultCard(record, employees.find { it.id == record.employeeId }, viewModel)
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.processPayrollForAll(month, year) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BusinessConfig.SuccessColor)
                ) {
                    Icon(Icons.Default.Groups, null)
                    Spacer(Modifier.width(8.dp))
                    Text("PROCESS ALL EMPLOYEES")
                }

                AnimatedVisibility(bulkSummary != null) {
                    bulkSummary?.let { summary ->
                        BulkResultPanel(summary, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationResultCard(record: PayrollEntity, employee: EmployeeEntity?, viewModel: PayrollViewModel) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, BusinessConfig.SuccessColor.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("CALCULATION RESULT", fontWeight = FontWeight.Black, color = BusinessConfig.SuccessColor, fontSize = 14.sp)
                IconButton(onClick = { viewModel.clearResults() }) { Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f)) }
            }
            Text("EMPLOYEE: ${employee?.fullName ?: "Unknown"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            
            SummaryRow("Basic Salary", formatAmount(record.basicSalary))
            record.allowanceDetails.forEach { allowance ->
                SummaryRow(allowance.name, formatAmount(allowance.calculatedAmount))
            }
            if (record.nonCashBenefits > 0) SummaryRow("Non-Cash Benefits", formatAmount(record.nonCashBenefits))
            if (record.valueOfQuarters > 0) SummaryRow("Value of Quarters", formatAmount(record.valueOfQuarters))
            
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
            SummaryRow("GROSS PAY", formatAmount(record.grossSalary), isBold = true)
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
            
            SummaryRow("SHA / SHIF", formatAmount(record.shifDeduction))
            SummaryRow("NSSF", formatAmount(record.nssfDeduction))
            SummaryRow("Housing Levy", formatAmount(record.housingLevyDeduction))
            SummaryRow("Tax Charged (Col L)", formatAmount(record.taxCharged))
            SummaryRow("Personal Relief", formatAmount(record.personalRelief))
            SummaryRow("Insurance Relief", formatAmount(record.insuranceRelief))
            SummaryRow("PAYE Tax (Col O)", formatAmount(record.payeAmount), isBold = true)
            
            record.deductionDetails.forEach { deduction ->
                SummaryRow(deduction.name, formatAmount(deduction.calculatedAmount))
            }
            if (record.loanRepayment > 0) SummaryRow("Staff Loan", formatAmount(record.loanRepayment))
            if (record.otherDeductions > 0) SummaryRow("Other Deductions", formatAmount(record.otherDeductions))
            
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
            SummaryRow("TOTAL DEDUCTIONS", formatAmount(record.grossSalary - record.netPay), isBold = true)
            SummaryRow("NET PAY", formatAmount(record.netPay), isBold = true, color = BusinessConfig.SuccessColor)
            
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (employee != null) viewModel.exportPayslip(employee, record) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(8.dp))
                    Text("PAYSLIP")
                }
                
                Button(
                    onClick = { viewModel.addToP9(record) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (record.status == "P9_ADDED") Color.Gray else BusinessConfig.SuccessColor
                    ),
                    enabled = record.status != "P9_ADDED"
                ) {
                    Icon(if (record.status == "P9_ADDED") Icons.Default.Check else Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (record.status == "P9_ADDED") "ADDED" else "ADD TO P9")
                }
            }
        }
    }
}

@Composable
fun BulkResultPanel(summary: BulkProcessSummary, viewModel: PayrollViewModel) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("BULK PROCESSING SUMMARY", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                IconButton(onClick = { viewModel.clearResults() }) { Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f)) }
            }
            Spacer(Modifier.height(8.dp))
            SummaryRow("Total Processed", summary.totalProcessed.toString())
            SummaryRow("Successful", summary.successCount.toString(), color = BusinessConfig.SuccessColor)
            SummaryRow("Failed", summary.failCount.toString(), color = if (summary.failCount > 0) Color.Red else Color.White)
            
            if (!summary.isComplete) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 16.dp))
            } else {
                Spacer(Modifier.height(16.dp))
                Text("Recently Processed:", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                summary.results.take(10).forEach { res ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Emp ID: ${res.employeeId.takeLast(6)}", color = Color.White, fontSize = 11.sp)
                        Text("Net: ${formatAmount(res.netPay)}", color = BusinessConfig.SuccessColor, fontSize = 11.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Go to 'Payslips' tab to view/print all generated documents.", fontSize = 11.sp, color = Color.Yellow)
            }
        }
    }
}

// --- PANEL B: PAYSLIPS ---
@Composable
fun PayslipsPanel(viewModel: PayrollViewModel) {
    val records by viewModel.currentMonthRecords.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    var filterMonth by remember { mutableStateOf(now.monthNumber) }
    var filterYear by remember { mutableStateOf(now.year) }

    LaunchedEffect(filterMonth, filterYear) {
        viewModel.loadPayrollRecords(filterMonth, filterYear)
    }

    GlassPanel(title = "Panel B: Payslips", icon = Icons.Default.Description) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filterMonth.toString(),
                    onValueChange = { filterMonth = it.toIntOrNull() ?: filterMonth },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = filterYear.toString(),
                    onValueChange = { filterYear = it.toIntOrNull() ?: filterYear },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            if (records.isEmpty()) {
                Text("No payroll records found for this period.", color = Color.Gray, modifier = Modifier.padding(16.dp))
            } else {
                records.forEach { record ->
                    val emp = employees.find { it.id == record.employeeId }
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(emp?.fullName ?: "Unknown Employee", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Gross: ${formatAmount(record.grossSalary)} | Net: ${formatAmount(record.netPay)}", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                            Row {
                                IconButton(onClick = { if (emp != null) viewModel.exportPayslip(emp, record) }) {
                                    Icon(Icons.Default.Visibility, "View", tint = Color.Cyan)
                                }
                                IconButton(onClick = { if (emp != null) viewModel.exportPayslip(emp, record) }) {
                                    Icon(Icons.Default.Print, "Print", tint = Color.White)
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { viewModel.exportAllPayslipsZip(filterMonth, filterYear) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FolderZip, null)
                    Spacer(Modifier.width(8.dp))
                    Text("PRINT ALL PAYSLIPS (ZIP)")
                }
            }
        }
    }
}

// --- PANEL C: P9 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P9Panel(viewModel: PayrollViewModel) {
    val employees by viewModel.employees.collectAsState()
    val p9Summary by viewModel.p9Summary.collectAsState()
    
    var year by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year) }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    GlassPanel(title = "Panel C: Annual P9", icon = Icons.Default.History) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = year.toString(),
                onValueChange = { year = it.toIntOrNull() ?: year },
                label = { Text("P9 Year") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Select Employee") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                val filtered = employees.filter { it.fullName.contains(searchQuery, ignoreCase = true) }
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    filtered.forEach { emp ->
                        DropdownMenuItem(
                            text = { Text(emp.fullName) },
                            onClick = {
                                selectedEmployeeId = emp.id
                                searchQuery = emp.fullName
                                isExpanded = false
                                viewModel.clearResults()
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { if (selectedEmployeeId.isNotEmpty()) viewModel.calculateP9(selectedEmployeeId, year) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedEmployeeId.isNotEmpty()
            ) {
                Icon(Icons.Default.Calculate, null)
                Spacer(Modifier.width(8.dp))
                Text("CALCULATE P9")
            }

            AnimatedVisibility(p9Summary != null) {
                p9Summary?.let { summary ->
                    val employee = employees.find { it.id == summary.employeeId }
                    val processedMonths = summary.months.filter { it.exists }
                    
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("P9 CALCULATION COMPLETE", fontWeight = FontWeight.Black, color = BusinessConfig.SuccessColor, fontSize = 12.sp)
                            Text("Employee: ${employee?.fullName ?: "Unknown"}", color = Color.White, fontWeight = FontWeight.Bold)
                            
                            Spacer(Modifier.height(8.dp))
                            val statusText = if (processedMonths.size == 12) "COMPLETE - 12 MONTHS AVAILABLE" 
                                            else "PARTIALLY COMPLETE - ${processedMonths.size} MONTHS AVAILABLE"
                            Text(statusText, color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            
                            Spacer(Modifier.height(12.dp))
                            // Monthly Table
                            P9MonthlyTable(summary)
                            
                            Spacer(Modifier.height(16.dp))
                            SummaryRow("ANNUAL TOTAL GROSS (Col D)", formatAmount(summary.totalD), isBold = true)
                            SummaryRow("ANNUAL TOTAL SHA (Col E2)", formatAmount(summary.totalE2))
                            SummaryRow("ANNUAL TOTAL NSSF (Col E3)", formatAmount(summary.totalE3))
                            SummaryRow("ANNUAL TOTAL HOUSING LEVY (Col E1)", formatAmount(summary.totalE1))
                            SummaryRow("ANNUAL TOTAL PAYE (Col O)", formatAmount(summary.totalO), isBold = true, color = BusinessConfig.SuccessColor)
                            
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { if (employee != null) viewModel.exportP9(employee.id, summary.year) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null)
                                Spacer(Modifier.width(8.dp))
                                Text("GENERATE P9 PDF")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun P9MonthlyTable(summary: P9AnnualSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(8.dp)) {
            // Header
            Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f)).padding(4.dp)) {
                Text("Month", Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Gross (D)", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("SHA (E2)", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("NSSF (E3)", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Levy (E1)", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("PAYE (O)", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            summary.months.forEach { month ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp)) {
                    Text(month.monthName.take(3), Modifier.weight(1.5f), fontSize = 10.sp, color = if (month.exists) Color.White else Color.Gray)
                    Text(if (month.exists) formatAmount(month.totalGrossPay) else "-", Modifier.weight(1f), fontSize = 10.sp, color = if (month.exists) Color.White else Color.Gray)
                    Text(if (month.exists) formatAmount(month.shif) else "-", Modifier.weight(1f), fontSize = 10.sp, color = if (month.exists) Color.White else Color.Gray)
                    Text(if (month.exists) formatAmount(month.nssf) else "-", Modifier.weight(1f), fontSize = 10.sp, color = if (month.exists) Color.White else Color.Gray)
                    Text(if (month.exists) formatAmount(month.ahl) else "-", Modifier.weight(1f), fontSize = 10.sp, color = if (month.exists) Color.White else Color.Gray)
                    Text(if (month.exists) formatAmount(month.payeTax) else "-", Modifier.weight(1f), fontSize = 10.sp, color = if (month.exists) BusinessConfig.SuccessColor else Color.Gray)
                }
            }
        }
    }
}

// --- PANEL D: P10 / CSV ---
@Composable
fun P10CsvPanel(viewModel: PayrollViewModel) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var month by remember { mutableStateOf(now.monthNumber) }
    var year by remember { mutableStateOf(now.year) }

    GlassPanel(title = "Panel D: P10 / PAYE CSV", icon = Icons.Default.TableChart) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = month.toString(),
                    onValueChange = { month = it.toIntOrNull() ?: month },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { year = it.toIntOrNull() ?: year },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            Button(
                onClick = { viewModel.exportKraCsv(month, year) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
            ) {
                Icon(Icons.Default.FileUpload, null)
                Spacer(Modifier.width(8.dp))
                Text("GENERATE PAYE CSV")
            }
            
            Button(
                onClick = { viewModel.exportP10Summary(month, year) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
            ) {
                Icon(Icons.Default.Summarize, null)
                Spacer(Modifier.width(8.dp))
                Text("GENERATE P10 SUMMARY")
            }
        }
    }
}

// --- PANEL E: PAYROLL SETTINGS ---
@Composable
fun PayrollSettingsPanel(viewModel: PayrollViewModel) {
    val rules by viewModel.statutoryRules.collectAsState()
    var showDetailedSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadStatutoryRules()
    }

    GlassPanel(title = "Panel E: Payroll Settings", icon = Icons.Default.Settings) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tax bands, relief rates, and contribution percentages are configured here.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Button(
                onClick = { showDetailedSettings = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Text("OPEN STATUTORY SETTINGS")
            }
        }
    }

    if (showDetailedSettings) {
        AlertDialog(
            onDismissRequest = { showDetailedSettings = false },
            title = { Text("Statutory Configuration") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Default Rules", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.restoreDefaultSettings() }) {
                            Icon(Icons.Default.Refresh, "Reset")
                        }
                    }
                    
                    rules.forEach { ruleWithBrackets ->
                        val rule = ruleWithBrackets.rule
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(rule.name, fontWeight = FontWeight.Bold)
                                    Switch(checked = rule.isActive, onCheckedChange = { viewModel.updateRule(rule.copy(isActive = it)) })
                                }
                                if (rule.type == "PERCENTAGE") {
                                    SettingsEditRow("Rate (%)", (rule.employeeRate * 100)) { viewModel.updateRule(rule.copy(employeeRate = it / 100.0)) }
                                } else if (rule.type == "FIXED") {
                                    SettingsEditRow("Amount (KES)", rule.employeeRate) { viewModel.updateRule(rule.copy(employeeRate = it)) }
                                }
                                
                                ruleWithBrackets.brackets.sortedBy { it.lowerLimit }.forEach { bracket ->
                                    val limitText = if (bracket.upperLimit >= 1000000000.0) "Above ${formatAmount(bracket.lowerLimit)}" else "${formatAmount(bracket.lowerLimit)} - ${formatAmount(bracket.upperLimit)}"
                                    Text("KES $limitText: ${formatAmount(bracket.rate * 100)}%", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showDetailedSettings = false }) { Text("Done") } }
        )
    }
}

// --- EMPLOYEE MANAGEMENT ---
@Composable
fun EmployeeManagementPanel(viewModel: PayrollViewModel) {
    val employees by viewModel.employees.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }

    GlassPanel(title = "Workforce Management", icon = Icons.Default.Badge) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            employees.forEach { employee ->
                ListItem(
                    headlineContent = { Text(employee.fullName, color = Color.White, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${employee.jobTitle} | PIN: ${employee.kraPin}", color = Color.White.copy(alpha = 0.7f)) },
                    trailingContent = {
                        IconButton(onClick = { selectedEmployee = employee }) {
                            Icon(Icons.Default.Edit, null, tint = Color.Cyan)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Register New Employee")
            }
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(onDismiss = { showAddDialog = false }, onConfirm = { viewModel.addEmployee(it) })
    }
    
    if (selectedEmployee != null) {
        EmployeeDetailSheet(
            employee = selectedEmployee!!,
            onDismiss = { selectedEmployee = null },
            onUpdate = { 
                viewModel.updateEmployee(it)
                selectedEmployee = null
            }
        )
    }
}

@Composable
fun GlassPanel(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.White) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = color.copy(alpha = 0.7f))
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, color = color)
    }
}

@Composable
fun AddEmployeeDialog(onDismiss: () -> Unit, onConfirm: (EmployeeEntity) -> Unit) {
    var name by remember { mutableStateOf("") }
    var empNo by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var kraPin by remember { mutableStateOf("") }
    var nssf by remember { mutableStateOf("") }
    var shif by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("") }
    var residentStatus by remember { mutableStateOf("Resident") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Employee") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = empNo, onValueChange = { empNo = it }, label = { Text("Employee Number") })
                OutlinedTextField(value = pos, onValueChange = { pos = it }, label = { Text("Position/Title") })
                OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") })
                OutlinedTextField(value = kraPin, onValueChange = { kraPin = it }, label = { Text("KRA PIN") })
                OutlinedTextField(value = nssf, onValueChange = { nssf = it }, label = { Text("NSSF Number") })
                OutlinedTextField(value = shif, onValueChange = { shif = it }, label = { Text("SHIF Number") })
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Basic Salary") })
                
                Text("Tax Status", fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = residentStatus == "Resident", onClick = { residentStatus = "Resident" })
                    Text("Resident", fontSize = 12.sp)
                    RadioButton(selected = residentStatus == "Non-Resident", onClick = { residentStatus = "Non-Resident" })
                    Text("Non-Resident", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val emp = EmployeeEntity(
                    fullName = name,
                    employeeNumber = empNo,
                    jobTitle = pos,
                    department = dept,
                    kraPin = kraPin,
                    nssfNumber = nssf,
                    shifNumber = shif,
                    basicSalary = salary.replace(",", "").toDoubleOrNull() ?: 0.0,
                    taxStatus = residentStatus,
                    status = "ACTIVE",
                    createdAt = Clock.System.now().toEpochMilliseconds()
                )
                onConfirm(emp)
                onDismiss()
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmployeeDetailSheet(employee: EmployeeEntity, onDismiss: () -> Unit, onUpdate: (EmployeeEntity) -> Unit) {
    var basicSalary by remember { mutableStateOf(formatAmount(employee.basicSalary)) }
    var sacco by remember { mutableStateOf(formatAmount(employee.saccoDeduction)) }
    var loanRepay by remember { mutableStateOf(formatAmount(employee.loanRepayment)) }
    var nonCash by remember { mutableStateOf(formatAmount(employee.nonCashBenefits)) }
    var quarters by remember { mutableStateOf(formatAmount(employee.valueOfQuarters)) }
    var mortgage by remember { mutableStateOf(formatAmount(employee.mortgageInterest)) }
    var prmf by remember { mutableStateOf(formatAmount(employee.prmfContribution)) }
    var otherPension by remember { mutableStateOf(formatAmount(employee.otherPension)) }
    
    // Dynamic lists
    val allowances = remember { mutableStateListOf<AllowanceEntity>().apply { addAll(employee.allowances) } }
    val deductions = remember { mutableStateListOf<DeductionEntity>().apply { addAll(employee.deductions) } }
    
    var showAddAllowance by remember { mutableStateOf(false) }
    var showAddDeduction by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${employee.fullName}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = basicSalary, onValueChange = { basicSalary = it }, label = { Text("Basic Salary") })
                OutlinedTextField(value = sacco, onValueChange = { sacco = it }, label = { Text("Monthly SACCO") })
                OutlinedTextField(value = loanRepay, onValueChange = { loanRepay = it }, label = { Text("Monthly Loan Repayment") })
                
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text("Allowances", fontWeight = FontWeight.Bold)
                allowances.forEachIndexed { index, allowance ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${allowance.name}: ${formatAmount(allowance.amount)}", Modifier.weight(1f), fontSize = 12.sp)
                        IconButton(onClick = { allowances.removeAt(index) }) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                    }
                }
                TextButton(onClick = { showAddAllowance = true }) { Icon(Icons.Default.Add, null); Text("Add Allowance") }

                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text("Other Deductions", fontWeight = FontWeight.Bold)
                deductions.forEachIndexed { index, deduction ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${deduction.name}: ${formatAmount(deduction.amount)}", Modifier.weight(1f), fontSize = 12.sp)
                        IconButton(onClick = { deductions.removeAt(index) }) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                    }
                }
                TextButton(onClick = { showAddDeduction = true }) { Icon(Icons.Default.Add, null); Text("Add Deduction") }

                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text("KRA Specifics", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = nonCash, onValueChange = { nonCash = it }, label = { Text("Non-Cash Benefits") })
                OutlinedTextField(value = quarters, onValueChange = { quarters = it }, label = { Text("Value of Quarters") })
                OutlinedTextField(value = mortgage, onValueChange = { mortgage = it }, label = { Text("Mortgage Interest") })
                OutlinedTextField(value = prmf, onValueChange = { prmf = it }, label = { Text("PRMF Contribution") })
                OutlinedTextField(value = otherPension, onValueChange = { otherPension = it }, label = { Text("Other Pension") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onUpdate(employee.copy(
                    basicSalary = basicSalary.replace(",", "").toDoubleOrNull() ?: employee.basicSalary,
                    saccoDeduction = sacco.replace(",", "").toDoubleOrNull() ?: employee.saccoDeduction,
                    loanRepayment = loanRepay.replace(",", "").toDoubleOrNull() ?: employee.loanRepayment,
                    nonCashBenefits = nonCash.replace(",", "").toDoubleOrNull() ?: employee.nonCashBenefits,
                    valueOfQuarters = quarters.replace(",", "").toDoubleOrNull() ?: employee.valueOfQuarters,
                    mortgageInterest = mortgage.replace(",", "").toDoubleOrNull() ?: employee.mortgageInterest,
                    prmfContribution = prmf.replace(",", "").toDoubleOrNull() ?: employee.prmfContribution,
                    otherPension = otherPension.replace(",", "").toDoubleOrNull() ?: employee.otherPension,
                    allowances = allowances.toList(),
                    deductions = deductions.toList()
                ))
            }) { Text("Save Profile") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showAddAllowance) {
        var aName by remember { mutableStateOf("") }
        var aAmt by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAllowance = false },
            title = { Text("New Allowance") },
            text = {
                Column {
                    OutlinedTextField(value = aName, onValueChange = { aName = it }, label = { Text("Name") })
                    OutlinedTextField(value = aAmt, onValueChange = { aAmt = it }, label = { Text("Amount") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    allowances.add(AllowanceEntity(name = aName, amount = aAmt.toDoubleOrNull() ?: 0.0))
                    showAddAllowance = false
                }) { Text("Add") }
            }
        )
    }

    if (showAddDeduction) {
        var dName by remember { mutableStateOf("") }
        var dAmt by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDeduction = false },
            title = { Text("New Deduction") },
            text = {
                Column {
                    OutlinedTextField(value = dName, onValueChange = { dName = it }, label = { Text("Name") })
                    OutlinedTextField(value = dAmt, onValueChange = { dAmt = it }, label = { Text("Amount") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    deductions.add(DeductionEntity(name = dName, amount = dAmt.toDoubleOrNull() ?: 0.0))
                    showAddDeduction = false
                }) { Text("Add") }
            }
        )
    }
}

@Composable
fun SettingsEditRow(label: String, value: Double, onUpdate: (Double) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(formatAmount(value)) }
    
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp)
        if (isEditing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = textValue, 
                    onValueChange = { textValue = it },
                    modifier = Modifier.width(100.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true
                )
                IconButton(onClick = { 
                    val newVal = textValue.replace(",", "").toDoubleOrNull() ?: value
                    onUpdate(newVal)
                    isEditing = false
                }) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                }
            }
        } else {
            TextButton(onClick = { isEditing = true }) {
                Text(formatAmount(value), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
