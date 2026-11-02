package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.rshotsbilling.viewmodel.ReceiptViewModel
import com.example.rshotsbilling.viewmodel.SalesViewModel
import com.example.rshotsbilling.viewmodel.ServiceViewModel
import com.example.rshotsbilling.viewmodel.StaffViewModel

import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesAndReceiptsHub(
    salesViewModel: SalesViewModel,
    serviceViewModel: ServiceViewModel,
    receiptViewModel: ReceiptViewModel,
    staffViewModel: StaffViewModel,
    customerViewModel: com.example.rshotsbilling.viewmodel.CustomerViewModel,
    onBack: () -> Unit
) {
    if (!UserSession.hasPermission(UserPermissions.CAN_SELL)) {
        AccessDeniedScreen(onBack)
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("New Sale", "Receipt History")

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Sales & Receipts Hub", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.White
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Row {
                                    Icon(if (index == 0) Icons.Default.PointOfSale else Icons.Default.History, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(title)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> SalesScreen(
                    salesViewModel = salesViewModel,
                    serviceViewModel = serviceViewModel,
                    receiptViewModel = receiptViewModel,
                    staffViewModel = staffViewModel,
                    customerViewModel = customerViewModel,
                    onBack = onBack
                )
                1 -> ReceiptHistoryScreen(
                    receiptViewModel = receiptViewModel,
                    salesViewModel = salesViewModel,
                    onBack = onBack
                )
            }
        }
    }
}
