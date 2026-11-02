package com.example.rshotsbilling.ui.payroll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity
import com.example.rshotsbilling.utils.PdfGenerator
import com.example.rshotsbilling.utils.PlatformReportUtils
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(
    employee: EmployeeEntity?,
    payroll: PayrollEntity?,
    onBack: () -> Unit
) {
    if (employee == null || payroll == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Employee or Payroll Data missing.")
        }
        return
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payslip - ${payroll.month}/${payroll.year}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        scope.launch {
                            val bytes = PdfGenerator.generatePayslip(employee, payroll)
                            // handle generated PDF bytes here if needed
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("EMPLOYEE PAYSLIP", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Name: ${employee.fullName}")
            Text("PIN: ${employee.kraPin}")
            Spacer(modifier = Modifier.height(24.dp))
            
            PayslipRow("Basic Salary", payroll.basicSalary)
            payroll.allowanceDetails.forEach { allowance ->
                PayslipRow(allowance.name, allowance.calculatedAmount)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            PayslipRow("GROSS PAY", payroll.grossSalary, isBold = true)
            
            Spacer(modifier = Modifier.height(16.dp))
            PayslipRow("NSSF", payroll.nssfDeduction)
            PayslipRow("SHA / SHIF", payroll.shifDeduction)
            PayslipRow("Housing Levy", payroll.housingLevyDeduction)
            PayslipRow("PAYE", payroll.payeAmount)
            
            payroll.deductionDetails.forEach { deduction ->
                PayslipRow(deduction.name, deduction.calculatedAmount)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            PayslipRow("NET PAY", payroll.netPay, isBold = true)
        }
    }
}

@Composable
fun PayslipRow(label: String, amount: Double, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(
            String.format(Locale.getDefault(), "%,.2f", amount),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
