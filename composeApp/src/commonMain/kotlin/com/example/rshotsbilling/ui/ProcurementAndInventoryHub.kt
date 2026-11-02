package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.viewmodel.InventoryViewModel
import com.example.rshotsbilling.viewmodel.PurchaseViewModel

import com.example.rshotsbilling.utils.UserPermissions
import com.example.rshotsbilling.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcurementAndInventoryHub(
    inventoryViewModel: InventoryViewModel,
    purchaseViewModel: PurchaseViewModel,
    onBack: () -> Unit,
    onNavigateInventory: (String) -> Unit
) {
    if (!UserSession.hasPermission(UserPermissions.CAN_MANAGE_INVENTORY)) {
        AccessDeniedScreen(onBack)
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Inventory", "Purchases")

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Procurement Hub", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp) },
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
                                    Icon(if (index == 0) Icons.Default.Inventory else Icons.Default.Payments, null)
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
                0 -> InventoryDashboard(
                    viewModel = inventoryViewModel,
                    onNavigate = onNavigateInventory
                )
                1 -> PurchasesScreen(
                    viewModel = purchaseViewModel,
                    onBack = onBack
                )
            }
        }
    }
}
