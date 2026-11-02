package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.utils.UserRole
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.FirebaseStatus
import com.example.rshotsbilling.utils.FirebaseState
import com.example.rshotsbilling.utils.KMPLogger
import com.example.rshotsbilling.viewmodel.LoginViewModel
import com.example.rshotsbilling.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    onLoginSuccess: (nextScreen: String) -> Unit
) {
    val viewModel = remember { LoginViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val firebaseState by FirebaseStatus.state.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Handle Navigation from ViewModel
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess((uiState as LoginUiState.Success).nextScreen)
        }
    }

    // UI Feedback for connection state
    val statusMessage = when (firebaseState) {
        FirebaseState.Uninitialized, FirebaseState.Initializing -> "Connecting to RShotsBilling services..."
        FirebaseState.Error -> "Connection failed. Please restart."
        else -> null
    }

    val errorMessage = (uiState as? LoginUiState.Error)?.message
    val isLoading = uiState is LoginUiState.Loading

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rshots Billing Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            statusMessage?.let {
                Text(it, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = androidx.compose.ui.graphics.Color.White) },
                enabled = !isLoading, // Removed dependency on firebaseState == Ready
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = androidx.compose.ui.graphics.Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading, // Removed dependency on firebaseState == Ready
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login(email, password)
                    }
                ),
                singleLine = true
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                enabled = !isLoading, // Removed dependency on firebaseState == Ready
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if(isLoading) "Authenticating..." else "Sign In")
            }

            TextButton(
                onClick = { viewModel.requestPasswordReset(email) },
                enabled = !isLoading,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Forgot Password? Request Reset", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }

            // Fallback for extreme cases
            if (!isLoading && errorMessage != null && (errorMessage.contains("TIMED OUT") || errorMessage.contains("Panic"))) {
                Button(onClick = { onLoginSuccess("loading_business") }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Force Login (Bypass Profile Fetch)")
                }
            }
        }
    }
}
