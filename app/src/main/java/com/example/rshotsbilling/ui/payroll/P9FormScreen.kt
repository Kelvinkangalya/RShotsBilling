package com.example.rshotsbilling.ui.payroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.utils.P9Generator
import com.example.rshotsbilling.utils.P9Calculator
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P9FormScreen(
    employee: EmployeeEntity?,
    records: List<PayrollEntity>,
    year: Int,
    onBack: () -> Unit
) {
    if (employee == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("P9 Form - $year") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        scope.launch {
                            val summary = P9Calculator.calculateAnnualP9(employee.id, year, records)
                            val bytes = P9Generator.generateP9Pdf(employee, summary)
                            // Here we should handle the bytes, but since we are in the app module,
                            // we'd typically call a platform handler. For now, just generate to fix compile.
                        }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, "Export PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // P9 Header
            P9Header(employee, year)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Table Header
            P9TableHeader()

            // Records
            LazyColumn(modifier = Modifier.weight(1f)) {
                items((1..12).toList()) { month ->
                    val record = records.find { it.month == month }
                    P9Row(month, record)
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
                
                item {
                    P9TotalsRow(records)
                }
            }
        }
    }
}

@Composable
fun P9Header(employee: EmployeeEntity, year: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("INCOME TAX DEPARTMENT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("P9 FORM - YEAR $year", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Employee's Name: ${employee.fullName}", fontSize = 12.sp)
                    Text("KRA PIN: ${employee.kraPin}", fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Employer's Name: ${BusinessConfig.COMPANY_NAME}", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun P9TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        P9HeaderCell("Month", 0.15f)
        P9HeaderCell("Basic", 0.15f)
        P9HeaderCell("Gross", 0.15f)
        P9HeaderCell("NSSF", 0.15f)
        P9HeaderCell("Taxable", 0.15f)
        P9HeaderCell("PAYE", 0.15f)
    }
}

@Composable
fun RowScope.P9HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
fun P9Row(month: Int, record: PayrollEntity?) {
    val monthName = Calendar.getInstance().apply { set(Calendar.MONTH, month - 1) }
        .getDisplayName(Calendar.SHORT, Calendar.LONG_FORMAT, Locale.getDefault()) ?: month.toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(monthName, modifier = Modifier.weight(0.15f), fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        P9DataCell(record?.basicSalary, 0.15f)
        P9DataCell(record?.grossSalary, 0.15f)
        P9DataCell(record?.nssfDeduction, 0.15f)
        P9DataCell(record?.taxablePay, 0.15f)
        P9DataCell(record?.payeAmount, 0.15f)
    }
}

@Composable
fun RowScope.P9DataCell(amount: Double?, weight: Float) {
    Text(
        text = if (amount != null) String.format(Locale.getDefault(), "%.0f", amount) else "-",
        modifier = Modifier.weight(weight),
        fontSize = 10.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
fun P9TotalsRow(records: List<PayrollEntity>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("TOTALS", modifier = Modifier.weight(0.15f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        P9TotalCell(records.sumOf { it.basicSalary }, 0.15f)
        P9TotalCell(records.sumOf { it.grossSalary }, 0.15f)
        P9TotalCell(records.sumOf { it.nssfDeduction }, 0.15f)
        P9TotalCell(records.sumOf { it.taxablePay }, 0.15f)
        P9TotalCell(records.sumOf { it.payeAmount }, 0.15f)
    }
}

@Composable
fun RowScope.P9TotalCell(amount: Double, weight: Float) {
    Text(
        text = String.format(Locale.getDefault(), "%.0f", amount),
        modifier = Modifier.weight(weight),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}
