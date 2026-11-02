package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.viewmodel.IdentityViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiagnosticsScreen(
    viewModel: IdentityViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Device Diagnostics", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncDevice() }, enabled = !uiState.isSyncing) {
                        if (uiState.isSyncing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Icon(Icons.Default.Refresh, "Sync", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SYNC STATUS CARD
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (uiState.lastSyncStatus == "Success") Icons.Default.CloudDone else Icons.Default.CloudOff,
                            null,
                            tint = if (uiState.lastSyncStatus == "Success") Color.Green else Color.Red
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Firestore Sync Status: ${uiState.lastSyncStatus}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Last Sync: ${formatTimestamp(profile.lastSync)}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            // DEVICE IDENTITY CARD
            DiagnosticSection("Installation Identity") {
                DiagnosticItem("Installation ID", profile.installationId)
                DiagnosticItem("Platform", profile.platform)
                DiagnosticItem("App Version", profile.appVersion)
            }

            // DEVICE INFO CARD
            DiagnosticSection("Device Information") {
                DiagnosticItem("Device Name", profile.deviceName)
                DiagnosticItem("OS Version", profile.osVersion)
                if (profile.manufacturer.isNotEmpty()) DiagnosticItem("Manufacturer", profile.manufacturer)
                if (profile.model.isNotEmpty()) DiagnosticItem("Model", profile.model)
                DiagnosticItem("First Install", formatTimestamp(profile.firstInstall))
            }

            // SESSION INFO CARD
            DiagnosticSection("Current Session") {
                DiagnosticItem("Firebase UID", profile.firebaseUid.ifEmpty { "Not Logged In" })
                DiagnosticItem("Business ID", profile.businessId.ifEmpty { "None" })
                DiagnosticItem("Current User", profile.currentUser.ifEmpty { "Unknown" })
                DiagnosticItem("Last Login", formatTimestamp(profile.lastLogin))
            }
        }
    }
}

@Composable
fun DiagnosticSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DiagnosticItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    return try {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        "Invalid Date"
    }
}
