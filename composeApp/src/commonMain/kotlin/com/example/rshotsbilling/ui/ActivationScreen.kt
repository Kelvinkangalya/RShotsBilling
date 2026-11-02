package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.getDeviceSerialNumber
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.getCurrentTimestamp
import com.example.rshotsbilling.model.StaffMember
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.launch

@Composable
fun ActivationScreen(
    onActivated: () -> Unit,
    onLogout: () -> Unit
) {
    var activationCode by remember { mutableStateOf("") }
    var businessCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val deviceSerial = remember { getDeviceSerialNumber() }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 400.dp)) {
            Text("System Activation", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Enter your activation details to proceed.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            
            Text("Device Serial: $deviceSerial", fontSize = 12.sp, fontWeight = FontWeight.Light)
            
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = businessCode,
                onValueChange = { businessCode = it },
                label = { Text("Business Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = activationCode,
                onValueChange = { activationCode = it },
                label = { Text("Activation Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (activationCode.isNotBlank() && businessCode.isNotBlank()) {
                        isLoading = true
                        errorMsg = null
                        println("DEBUG: Starting activation check for business: $businessCode")
                        scope.launch {
                            try {
                                val db = FirestoreManager.db
                                val email = UserSession.userEmail ?: throw Exception("Not logged in")
                                
                                println("DEBUG: Querying activationCodes collection...")
                                val codeSnapshot = db.collection("activationCodes")
                                    .where { "code" equalTo activationCode }
                                    .where { "used" equalTo false }
                                    .get()
                                
                                if (codeSnapshot.documents.isNotEmpty()) {
                                    val codeDoc = codeSnapshot.documents.first()
                                    val codeData = codeDoc.data(com.example.rshotsbilling.model.ActivationCode.serializer())
                                    
                                    if (codeData.companyId != businessCode && businessCode.isNotEmpty()) {
                                        errorMsg = "Invalid Business Code for this Activation Code."
                                        isLoading = false
                                        return@launch
                                    }

                                    println("DEBUG: Valid code found for companyId: ${codeData.companyId}")
                                    
                                    // Register Device
                                    val device = com.example.rshotsbilling.model.DeviceRegistration(
                                        deviceId = deviceSerial,
                                        installationId = "inst_${getCurrentTimestamp()}",
                                        deviceName = "Device-${deviceSerial.takeLast(4)}",
                                        platform = "KMP",
                                        status = "Approved", // Auto-approve for valid code for now
                                        businessId = codeData.companyId,
                                        userId = email,
                                        registrationDate = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                                    )
                                    
                                    println("DEBUG: Registering device $deviceSerial in Firestore...")
                                    db.collection("companies")
                                        .document(codeData.companyId)
                                        .collection("devices")
                                        .document(deviceSerial)
                                        .set(com.example.rshotsbilling.model.DeviceRegistration.serializer(), device)

                                    // Mark code as used
                                    db.collection("activationCodes").document(codeDoc.id).update(
                                        "used" to true,
                                        "usedBy" to email,
                                        "usedDate" to getCurrentTimestamp()
                                    )

                                    UserSession.businessId = codeData.companyId
                                    UserSession.companyId = codeData.companyId
                                    UserSession.isDeviceActivated = true
                                    
                                    println("DEBUG: Activation successful.")
                                    onActivated()
                                } else {
                                    println("DEBUG: No valid unused code found.")
                                    errorMsg = "Invalid or expired Activation Code."
                                }
                            } catch (e: Exception) {
                                println("DEBUG: Activation error: ${e.message}")
                                e.printStackTrace()
                                errorMsg = e.message ?: "Activation failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && activationCode.isNotBlank() && businessCode.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("ACTIVATE")
            }
            
            TextButton(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
                Text("Log Out")
            }
        }
    }
}
