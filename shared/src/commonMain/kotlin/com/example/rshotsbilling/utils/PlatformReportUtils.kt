package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity

expect object PlatformReportUtils {
    fun exportExcel(sales: List<Sale>, purchases: List<Purchase>, range: String)
    fun exportPdf(sales: List<Sale>, purchases: List<Purchase>, totalSales: Double, totalExpenses: Double, profit: Double)
    fun exportPayslip(employee: EmployeeEntity, payroll: PayrollEntity)
    fun exportP9(employee: EmployeeEntity, summary: P9AnnualSummary)
    fun exportPayrollExcel(employees: Map<String, EmployeeEntity>, records: List<PayrollEntity>, month: Int, year: Int)
    fun exportStatutoryReport(title: String, employees: Map<String, EmployeeEntity>, records: List<PayrollEntity>, month: Int, year: Int)
    fun saveCsv(content: String, fileName: String)
    fun exportZip(files: List<Pair<String, ByteArray>>, zipFileName: String)
    fun exportAllPayslips(records: List<PayrollEntity>, employees: Map<String, EmployeeEntity>, month: Int, year: Int, asZip: Boolean)
    fun exportPayrollRegisterPdf(records: List<PayrollEntity>, employees: Map<String, EmployeeEntity>, month: Int, year: Int)
}

expect fun formatAmount(value: Double, digits: Int = 2): String
