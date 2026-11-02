package com.example.rshotsbilling.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.ui.components.AnalyticsCard
import com.example.rshotsbilling.utils.BusinessConfig
import com.example.rshotsbilling.viewmodel.InventoryViewModel

@Composable
fun InventoryDashboard(
    viewModel: InventoryViewModel,
    onNavigate: (String) -> Unit
) {
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val outOfStockCount by viewModel.outOfStockCount.collectAsState()
    val services by viewModel.services.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Inventory Health",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsCard(
                title = "TOTAL PRODUCTS",
                value = services.size.toString(),
                subtitle = "Active Catalog",
                color = BusinessConfig.PrimaryColor,
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "LOW STOCK",
                value = lowStockCount.toString(),
                subtitle = "Alert Level",
                color = BusinessConfig.WarningColor,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsCard(
                title = "OUT OF STOCK",
                value = outOfStockCount.toString(),
                subtitle = "Immediate Action",
                color = BusinessConfig.ErrorColor,
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "VALUATION",
                value = "${BusinessConfig.CURRENCY} --",
                subtitle = "Stock Value",
                color = BusinessConfig.SuccessColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Inventory Controls",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))

        val menuItems = listOf(
            InventoryMenuItem("Stock Ledger", "inventory_ledger", Icons.Default.MenuBook),
            InventoryMenuItem("Batch & Expiry", "batch_management", Icons.Default.History),
            InventoryMenuItem("Expiry Alerts", "expiry_alerts", Icons.Default.NotificationsActive),
            InventoryMenuItem("Procurement", "procurement", Icons.Default.ShoppingCart),
            InventoryMenuItem("Suppliers", "suppliers", Icons.Default.Business),
            InventoryMenuItem("Adjustments", "stock_adjustments", Icons.Default.EditAttributes),
            InventoryMenuItem("Reports", "inventory_reports", Icons.Default.Assessment)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onNavigate(item.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

data class InventoryMenuItem(val title: String, val route: String, val icon: ImageVector)
