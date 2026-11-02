package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.payroll.*
import com.example.rshotsbilling.repository.PayrollRepository
import com.example.rshotsbilling.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class BulkProcessSummary(
    val totalProcessed: Int = 0,
    val successCount: Int = 0,
    val failCount: Int = 0,
    val isComplete: Boolean = false,
    val results: List<PayrollEntity> = emptyList()
)

class PayrollViewModel : ViewModel() {
    private val repository = PayrollRepository()

    private val _employees = MutableStateFlow<List<EmployeeEntity>>(emptyList())
    val employees: StateFlow<List<EmployeeEntity>> = _employees.asStateFlow()

    private val _currentMonthRecords = MutableStateFlow<List<PayrollEntity>>(emptyList())
    val currentMonthRecords: StateFlow<List<PayrollEntity>> = _currentMonthRecords.asStateFlow()

    private val _statutoryRules = MutableStateFlow<List<RuleWithBrackets>>(emptyList())
    val statutoryRules: StateFlow<List<RuleWithBrackets>> = _statutoryRules.asStateFlow()

    private val _processedResult = MutableStateFlow<PayrollEntity?>(null)
    val processedResult: StateFlow<PayrollEntity?> = _processedResult.asStateFlow()

    private val _bulkSummary = MutableStateFlow<BulkProcessSummary?>(null)
    val bulkSummary: StateFlow<BulkProcessSummary?> = _bulkSummary.asStateFlow()

    private val _p9Summary = MutableStateFlow<P9AnnualSummary?>(null)
    val p9Summary: StateFlow<P9AnnualSummary?> = _p9Summary.asStateFlow()

    fun loadEmployees() {
        viewModelScope.launch {
            repository.getEmployeesFlow().collect {
                _employees.value = it
            }
        }
    }

    fun loadPayrollRecords(month: Int, year: Int) {
        viewModelScope.launch {
            repository.getPayrollRecordsFlow(month, year).collect {
                _currentMonthRecords.value = it
            }
        }
    }

    fun loadStatutoryRules() {
        viewModelScope.launch {
            repository.getRulesFlow().collect {
                _statutoryRules.value = it
            }
        }
    }

    fun updateRule(rule: StatutoryDeductionRuleEntity) {
        viewModelScope.launch {
            repository.updateRule(rule)
            loadStatutoryRules()
        }
    }

    fun addRule(rule: StatutoryDeductionRuleEntity) {
        viewModelScope.launch {
            repository.addRule(rule)
            loadStatutoryRules()
        }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch {
            repository.deleteRule(ruleId)
            loadStatutoryRules()
        }
    }

    fun restoreDefaultSettings() {
        viewModelScope.launch {
            repository.seedDefaultStatutoryRules()
            loadStatutoryRules()
        }
    }

    fun addEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.addEmployee(employee)
        }
    }

    fun updateEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
        }
    }

    fun processPayrollForEmployee(employeeId: String, month: Int, year: Int) {
        viewModelScope.launch {
            _processedResult.value = null
            val result = repository.processPayroll(employeeId, month, year)
            _processedResult.value = result
            if (result != null) {
                loadPayrollRecords(month, year)
            }
        }
    }

    fun processPayrollForAll(month: Int, year: Int) {
        viewModelScope.launch {
            _bulkSummary.value = BulkProcessSummary(totalProcessed = employees.value.size)
            val success = mutableListOf<PayrollEntity>()
            var fail = 0
            
            employees.value.forEach { emp ->
                val result = repository.processPayroll(emp.id, month, year)
                if (result != null) {
                    success.add(result)
                } else {
                    fail++
                }
                _bulkSummary.value = _bulkSummary.value?.copy(
                    successCount = success.size,
                    failCount = fail,
                    results = success.toList()
                )
            }
            _bulkSummary.value = _bulkSummary.value?.copy(isComplete = true)
            loadPayrollRecords(month, year)
        }
    }

    fun clearResults() {
        _processedResult.value = null
        _bulkSummary.value = null
        _p9Summary.value = null
    }

    fun addToP9(record: PayrollEntity) {
        viewModelScope.launch {
            KMPLogger.log("P9 IMPORT: empId=${record.employeeId}, month=${record.month}, year=${record.year}")
            repository.updatePayrollStatus(record.id, "P9_ADDED")
            // Refresh P9 summary if it was loaded
            p9Summary.value?.let { current ->
                if (current.employeeId == record.employeeId && current.year == record.year) {
                    calculateP9(record.employeeId, record.year)
                }
            }
            // Update the processedResult if it matches
            if (_processedResult.value?.id == record.id) {
                _processedResult.value = record.copy(status = "P9_ADDED")
            }
        }
    }

    fun calculateP9(employeeId: String, year: Int) {
        viewModelScope.launch {
            KMPLogger.log("ViewModel: Calculating P9 for $employeeId, year $year")
            val history = repository.getEmployeePayrollHistory(employeeId, year)
            // Strict status filtering for P9
            val p9Records = history.filter { it.status == "P9_ADDED" || it.status == "FINALIZED" || it.status == "APPROVED" }
            KMPLogger.log("ViewModel: Found ${history.size} total records, using ${p9Records.size} for P9 (Status-filtered)")
            _p9Summary.value = P9Calculator.calculateAnnualP9(employeeId, year, p9Records)
        }
    }

    fun approvePayroll(month: Int, year: Int) {
        viewModelScope.launch {
            currentMonthRecords.value.forEach { record ->
                if (record.status == "PROCESSED" || record.status == "FINALIZED") {
                    repository.updatePayrollStatus(record.id, "APPROVED")
                }
            }
        }
    }

    fun exportPayslip(employee: EmployeeEntity, payroll: PayrollEntity) {
        KMPLogger.log("ViewModel: Exporting Payslip for ${employee.fullName}")
        PlatformReportUtils.exportPayslip(employee, payroll)
    }

    fun exportAllPayslipsZip(month: Int, year: Int) {
        viewModelScope.launch {
            val recs = repository.getPayrollRecords(month, year)
            val empMap = employees.value.associateBy { it.id }
            if (recs.isNotEmpty()) {
                PlatformReportUtils.exportAllPayslips(recs, empMap, month, year, true)
            }
        }
    }

    fun exportP9(employeeId: String, year: Int) {
        viewModelScope.launch {
            KMPLogger.log("ViewModel: Exporting P9 for $employeeId, year $year")
            val history = repository.getEmployeePayrollHistory(employeeId, year)
            val p9Records = history.filter { it.status == "P9_ADDED" || it.status == "FINALIZED" || it.status == "APPROVED" }
            val employee = employees.value.find { it.id == employeeId }
            if (employee != null && p9Records.isNotEmpty()) {
                val summary = P9Calculator.calculateAnnualP9(employeeId, year, p9Records)
                PlatformReportUtils.exportP9(employee, summary)
            } else {
                KMPLogger.log("ViewModel: P9 Export ABORTED. history empty or no finalized records? ${p9Records.isEmpty()}")
            }
        }
    }

    fun exportKraCsv(month: Int, year: Int) {
        viewModelScope.launch {
            val recs = repository.getPayrollRecords(month, year)
            val empMap = employees.value.associateBy { it.id }
            if (recs.isNotEmpty()) {
                val csvContent = PayrollReportExporter.generateP10Csv(empMap, recs, true)
                PlatformReportUtils.saveCsv(csvContent, "KRA_P10_Export_${month}_${year}.csv")
            }
        }
    }

    fun exportP10Summary(month: Int, year: Int) {
        viewModelScope.launch {
            val recs = repository.getPayrollRecords(month, year)
            val empMap = employees.value.associateBy { it.id }
            if (recs.isNotEmpty()) {
                PlatformReportUtils.exportStatutoryReport("P10_Employer_Return", empMap, recs, month, year)
            }
        }
    }

    fun exportPayrollRegisterPdf(month: Int, year: Int) {
        viewModelScope.launch {
            val recs = repository.getPayrollRecords(month, year)
            val empMap = employees.value.associateBy { it.id }
            if (recs.isNotEmpty()) {
                PlatformReportUtils.exportPayrollRegisterPdf(recs, empMap, month, year)
            }
        }
    }

    fun exportPayrollRegisterExcel(month: Int, year: Int) {
        viewModelScope.launch {
            val recs = repository.getPayrollRecords(month, year)
            val empMap = employees.value.associateBy { it.id }
            if (recs.isNotEmpty()) {
                PlatformReportUtils.exportPayrollExcel(empMap, recs, month, year)
            }
        }
    }
}
