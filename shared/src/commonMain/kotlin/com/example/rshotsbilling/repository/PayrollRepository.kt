package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.payroll.*
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.PayrollCalculationEngine
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.KMPLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*
import kotlinx.datetime.Clock

class PayrollRepository {
    private val db by lazy {
        KMPLogger.log("DEBUG: PayrollRepository initialized.")
        FirestoreManager.provideDb()
    }
    private val employeesRef get() = FirestoreManager.getEmployeesRef()
    private val payrollRef get() = FirestoreManager.getPayrollRecordsRef()
    private val rulesRef get() = FirestoreManager.getStatutoryRulesRef()
    private val bracketsRef get() = FirestoreManager.getStatutoryBracketsRef()

    fun getEmployeesFlow(): Flow<List<EmployeeEntity>> {
        val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
        KMPLogger.log("[PERF] Employees query flow START")
        return employeesRef.snapshots().map { snapshot ->
            val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
            KMPLogger.log("[PERF] Employees query flow received: $duration ms. Documents: ${snapshot.documents.size}")
            snapshot.documents.map { it.data(EmployeeEntity.serializer()) }
        }
    }

    suspend fun getEmployee(id: String): EmployeeEntity? {
        val doc = employeesRef.document(id).get()
        return if (doc.exists) doc.data(EmployeeEntity.serializer()) else null
    }

    suspend fun addEmployee(employee: EmployeeEntity) {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val id = if (employee.id.isEmpty()) "emp_$now" else employee.id
        val newEmployee = employee.copy(id = id, createdAt = now)
        employeesRef.document(id).set(EmployeeEntity.serializer(), newEmployee)
    }

    suspend fun updateEmployee(employee: EmployeeEntity) {
        if (employee.id.isEmpty()) return
        employeesRef.document(employee.id).set(EmployeeEntity.serializer(), employee)
    }

    fun getPayrollRecordsFlow(month: Int, year: Int): Flow<List<PayrollEntity>> = 
        payrollRef.where { "month" equalTo month }.where { "year" equalTo year }.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(PayrollEntity.serializer()) }
        }

    suspend fun getPayrollRecords(month: Int, year: Int): List<PayrollEntity> {
        val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
        KMPLogger.log("[PERF] Payroll records query START for $month/$year")
        val snapshot = payrollRef.where { "month" equalTo month }.where { "year" equalTo year }.get()
        val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
        KMPLogger.log("[PERF] Payroll records query END: $duration ms. Documents: ${snapshot.documents.size}")
        return snapshot.documents.map { it.data(PayrollEntity.serializer()) }
    }

    suspend fun getEmployeePayrollHistory(employeeId: String, year: Int): List<PayrollEntity> {
        KMPLogger.log("DEBUG: getEmployeePayrollHistory START for empId: $employeeId, year: $year")
        val snapshot = payrollRef.where { "employeeId" equalTo employeeId }.where { "year" equalTo year }.get()
        val records = snapshot.documents.map { it.data(PayrollEntity.serializer()) }.sortedBy { it.month }
        KMPLogger.log("DEBUG: getEmployeePayrollHistory END. Found ${records.size} records.")
        return records
    }

    suspend fun getPayrollRecord(id: String): PayrollEntity? {
        val doc = payrollRef.document(id).get()
        return if (doc.exists) doc.data(PayrollEntity.serializer()) else null
    }

    suspend fun updatePayrollStatus(recordId: String, status: String) {
        payrollRef.document(recordId).update("status" to status)
    }

    suspend fun addPayrollRecord(record: PayrollEntity): PayrollEntity {
        val now = Clock.System.now().toEpochMilliseconds()
        val id = "pay_${record.month}_${record.year}_${record.employeeId}"
        val newRecord = record.copy(id = id, processedAt = now)
        KMPLogger.log("PAYROLL SAVE: id=$id, empId=${record.employeeId}, month=${record.month}, year=${record.year}, gross=${record.grossSalary}, sha=${record.shifDeduction}, nssf=${record.nssfDeduction}, levy=${record.housingLevyDeduction}, paye=${record.payeAmount}, net=${record.netPay}")
        payrollRef.document(id).set(PayrollEntity.serializer(), newRecord)
        return newRecord
    }

    suspend fun getRulesWithBrackets(): List<RuleWithBrackets> {
        val rulesSnapshot = rulesRef.get()
        val rules = rulesSnapshot.documents.map { it.data(StatutoryDeductionRuleEntity.serializer()) }
        val bracketsSnapshot = bracketsRef.get()
        val allBrackets = bracketsSnapshot.documents.map { it.data(StatutoryBracketEntity.serializer()) }
        
        return rules.map { rule ->
            RuleWithBrackets(
                rule = rule,
                brackets = allBrackets.filter { it.ruleId == rule.id }
            )
        }
    }

    fun getRulesFlow(): kotlinx.coroutines.flow.Flow<List<RuleWithBrackets>> = kotlinx.coroutines.flow.flow {
        val rules = getRulesWithBrackets()
        if (rules.isEmpty()) {
            seedDefaultStatutoryRules()
            emit(getRulesWithBrackets())
        } else {
            emit(rules)
        }
    }

    suspend fun updateRule(rule: StatutoryDeductionRuleEntity) {
        rulesRef.document(rule.id).set(StatutoryDeductionRuleEntity.serializer(), rule)
    }

    suspend fun addRule(rule: StatutoryDeductionRuleEntity) {
        val id = if (rule.id.isEmpty()) "rule_${Clock.System.now().toEpochMilliseconds()}" else rule.id
        rulesRef.document(id).set(StatutoryDeductionRuleEntity.serializer(), rule.copy(id = id))
    }

    suspend fun deleteRule(id: String) {
        rulesRef.document(id).delete()
    }

    suspend fun seedDefaultStatutoryRules() {
        KMPLogger.log("DEBUG: Seeding default statutory rules...")
        val rules = listOf(
            StatutoryDeductionRuleEntity(id = "nssf", name = "NSSF", type = "BRACKET", employeeRate = 0.06, employerRate = 0.06, category = "STATUTORY"),
            StatutoryDeductionRuleEntity(id = "shif", name = "SHIF", type = "PERCENTAGE", employeeRate = 0.0275, employerRate = 0.0, category = "STATUTORY"),
            StatutoryDeductionRuleEntity(id = "shif_min", name = "SHIF_MIN", type = "FIXED", employeeRate = 300.0, category = "STATUTORY"),
            StatutoryDeductionRuleEntity(id = "ahl", name = "HOUSING_LEVY", type = "PERCENTAGE", employeeRate = 0.015, employerRate = 0.015, category = "STATUTORY"),
            StatutoryDeductionRuleEntity(id = "paye", name = "PAYE", type = "BRACKET", category = "STATUTORY"),
            StatutoryDeductionRuleEntity(id = "personal_relief", name = "PERSONAL_RELIEF", type = "FIXED", employeeRate = 2400.0, category = "STATUTORY")
        )

        val brackets = listOf(
            // NSSF (Feb 2025 Tier II)
            StatutoryBracketEntity(id = "nssf_1", ruleId = "nssf", lowerLimit = 0.0, upperLimit = 8000.0, rate = 0.06),
            StatutoryBracketEntity(id = "nssf_2", ruleId = "nssf", lowerLimit = 8000.0, upperLimit = 72000.0, rate = 0.06),
            
            // PAYE (2025)
            StatutoryBracketEntity(id = "paye_1", ruleId = "paye", lowerLimit = 0.0, upperLimit = 24000.0, rate = 0.10),
            StatutoryBracketEntity(id = "paye_2", ruleId = "paye", lowerLimit = 24000.0, upperLimit = 32333.0, rate = 0.25),
            StatutoryBracketEntity(id = "paye_3", ruleId = "paye", lowerLimit = 32333.0, upperLimit = 500000.0, rate = 0.30),
            StatutoryBracketEntity(id = "paye_4", ruleId = "paye", lowerLimit = 500000.0, upperLimit = 800000.0, rate = 0.325),
            StatutoryBracketEntity(id = "paye_5", ruleId = "paye", lowerLimit = 800000.0, upperLimit = 1000000000.0, rate = 0.35)
        )

        rules.forEach { rulesRef.document(it.id).set(StatutoryDeductionRuleEntity.serializer(), it) }
        brackets.forEach { bracketsRef.document(it.id).set(StatutoryBracketEntity.serializer(), it) }
    }

    suspend fun processPayroll(employeeId: String, month: Int, year: Int): PayrollEntity? {
        KMPLogger.log("PAYROLL CALC START: empId=$employeeId, $month/$year")
        return try {
            val employeeSnapshot = employeesRef.document(employeeId).get()
            if (!employeeSnapshot.exists) {
                KMPLogger.log("ERROR: Employee $employeeId not found")
                return null
            }
            val employee = employeeSnapshot.data(EmployeeEntity.serializer())
            
            var rules = getRulesWithBrackets()
            val mandatoryNames = listOf("NSSF", "SHIF", "HOUSING_LEVY", "PAYE", "PERSONAL_RELIEF")
            
            // Validate presence and completeness (e.g., brackets for PAYE)
            val existingNames = rules.map { it.rule.name }
            val payeBrackets = rules.find { it.rule.name == "PAYE" }?.brackets ?: emptyList()
            
            if (rules.isEmpty() || !mandatoryNames.all { existingNames.contains(it) } || payeBrackets.isEmpty()) {
                KMPLogger.log("DEBUG: Rules incomplete or old. Force re-seeding...")
                seedDefaultStatutoryRules()
                rules = getRulesWithBrackets()
            }
            
            val result = PayrollCalculationEngine.calculate(
                basicSalary = employee.basicSalary,
                allowances = employee.allowances, 
                deductions = employee.deductions,
                rules = rules,
                nonCashBenefits = employee.nonCashBenefits,
                valueOfQuarters = employee.valueOfQuarters,
                mortgageInterest = employee.mortgageInterest,
                prmf = employee.prmfContribution,
                otherPension = employee.otherPension,
                isResident = employee.taxStatus == "Resident",
                isHousingLevyExempt = employee.housingLevyExempt,
                loanRepayment = employee.loanRepayment,
                salaryAdvance = 0.0 
            )
            
            val finalNetPay = result.netPay - employee.saccoDeduction
            
            val record = PayrollEntity(
                employeeId = employee.id,
                month = month,
                year = year,
                grossSalary = result.grossPay,
                basicSalary = employee.basicSalary,
                nssfDeduction = result.nssfEmployee,
                shifDeduction = result.shifEmployee,
                housingLevyDeduction = result.housingLevyEmployee,
                employerNssf = result.nssfEmployer,
                employerShif = result.shifEmployer,
                employerHousingLevy = result.housingLevyEmployer,
                taxablePay = result.taxableIncome,
                taxCharged = result.taxCharged,
                payeAmount = result.payeTax,
                personalRelief = result.personalRelief,
                insuranceRelief = result.insuranceRelief,
                mortgageInterest = employee.mortgageInterest,
                prmfContribution = employee.prmfContribution,
                otherPension = employee.otherPension,
                nonCashBenefits = employee.nonCashBenefits,
                valueOfQuarters = employee.valueOfQuarters,
                allowanceDetails = result.allowanceDetails,
                deductionDetails = result.deductionDetails,
                loanRepayment = employee.loanRepayment,
                otherDeductions = employee.saccoDeduction,
                totalEmployerContribution = result.totalEmployerContribution,
                netPay = finalNetPay,
                status = "FINALIZED",
                processedBy = UserSession.displayName ?: "System",
                processedAt = Clock.System.now().toEpochMilliseconds()
            )
            
            KMPLogger.log("PAYROLL CALC END: Net=${record.netPay}, Gross=${record.grossSalary}, SHA=${record.shifDeduction}, NSSF=${record.nssfDeduction}, Levy=${record.housingLevyDeduction}, PAYE=${record.payeAmount}")
            val savedRecord = addPayrollRecord(record)
            savedRecord
        } catch (e: Exception) {
            KMPLogger.log("ERROR processing payroll for $employeeId: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun clockIn(employeeId: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        val id = "attn_$now"
        val attendance = AttendanceEntity(id = id, employeeId = employeeId, date = now, clockInTime = now, status = "PRESENT")
        FirestoreManager.getAttendanceRecordsRef().document(id).set(AttendanceEntity.serializer(), attendance)
    }

    suspend fun applyLeave(leave: LeaveEntity) {
        val now = Clock.System.now().toEpochMilliseconds()
        val id = if (leave.id.isEmpty()) "leave_$now" else leave.id
        FirestoreManager.getLeaveRecordsRef().document(id).set(LeaveEntity.serializer(), leave.copy(id = id, createdAt = now))
    }

    fun getPendingLeavesFlow(): Flow<List<LeaveEntity>> = FirestoreManager.getLeaveRecordsRef().where { "status" equalTo "PENDING" }.snapshots().map { snapshot ->
        snapshot.documents.map { it.data(LeaveEntity.serializer()) }
    }
}
