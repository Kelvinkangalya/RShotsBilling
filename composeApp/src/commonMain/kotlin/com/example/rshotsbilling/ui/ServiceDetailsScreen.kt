@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rshotsbilling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rshotsbilling.viewmodel.ServiceViewModel

@Composable
fun ServiceDetailsScreen(
    category: String,
    viewModel: ServiceViewModel,
    onBack: () -> Unit
) {
    val services by viewModel.services.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadServices()
    }

    val filteredServices = services.filter {
        it.category == category
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text(
                text = "Services under $category",
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.ui.graphics.Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredServices.isEmpty()) {
                Text(
                    text = "No services found.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LazyColumn {
                items(filteredServices) { service ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = service.description
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Ksh ${service.price}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.deleteService(service.id)
                                }
                            ) {
                                Text("Delete Service")
                            }
                        }
                    }
                }
            }
        }
    }
}
