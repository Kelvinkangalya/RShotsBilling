package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.FirestoreManager
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun PasswordChangeScreen(
    onSuccess: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 400.dp)) {
            Text("Security Update", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("You are using a temporary password. Please set a new secure password to continue.", fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        errorMsg = "Password must be at least 6 characters"
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMsg = "Passwords do not match"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMsg = null
                    scope.launch {
                        try {
                            val user = Firebase.auth.currentUser ?: throw Exception("User not authenticated")
                            user.updatePassword(newPassword)
                            
                            // Update Firestore record to clear temp flag
                            val db = FirestoreManager.db
                            val staffId = UserSession.userEmail ?: user.email ?: ""
                            
                            // We need the document ID. In our system, we use email or a specific ID.
                            // Let's assume we find the staff member by email in their company.
                            val companyId = UserSession.companyId
                            val staffSnapshot = db.collection("companies").document(companyId)
                                .collection("staff")
                                .where { "email" equalTo staffId }
                                .get()
                            
                            if (staffSnapshot.documents.isNotEmpty()) {
                                val doc = staffSnapshot.documents.first()
                                db.collection("companies").document(companyId)
                                    .collection("staff").document(doc.id)
                                    .update("isTemporaryPassword" to false)
                            }
                            
                            onSuccess()
                        } catch (e: Exception) {
                            errorMsg = e.message ?: "Failed to update password"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("UPDATE PASSWORD")
            }
        }
    }
}
