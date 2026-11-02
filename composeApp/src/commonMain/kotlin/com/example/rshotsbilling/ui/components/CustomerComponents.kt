package com.example.rshotsbilling.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rshotsbilling.model.CustomerEntity

@Composable
fun AddCustomerDialog(
    initialCustomer: CustomerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var email by remember { mutableStateOf(initialCustomer?.email ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var kraPin by remember { mutableStateOf(initialCustomer?.kraPin ?: "") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }
    
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCustomer == null) "Add New Customer" else "Edit Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Full Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { phone = it }, 
                    label = { Text("Phone Number") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email (Optional)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address, 
                    onValueChange = { address = it }, 
                    label = { Text("Physical Address") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                    singleLine = true
                )
                OutlinedTextField(
                    value = kraPin, 
                    onValueChange = { kraPin = it }, 
                    label = { Text("KRA PIN") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes, 
                    onValueChange = { notes = it }, 
                    label = { Text("Notes") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            onSave((initialCustomer ?: CustomerEntity()).copy(
                                name = name,
                                phone = phone,
                                email = email,
                                address = address,
                                kraPin = kraPin,
                                notes = notes
                            ))
                        }
                    })
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave((initialCustomer ?: CustomerEntity()).copy(
                        name = name,
                        phone = phone,
                        email = email,
                        address = address,
                        kraPin = kraPin,
                        notes = notes
                    ))
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text(if (initialCustomer == null) "SAVE & CONTINUE" else "UPDATE CUSTOMER")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
