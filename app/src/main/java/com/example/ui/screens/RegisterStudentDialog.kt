package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Student

@Composable
fun RegisterStudentDialog(
    initialStudent: Student? = null,
    currencySymbol: String = "Rs. ",
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        contact: String,
        address: String,
        parentContact: String,
        fee: Double,
        markPaid: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var contactNumber by remember { mutableStateOf(initialStudent?.contactNumber ?: "") }
    var address by remember { mutableStateOf(initialStudent?.address ?: "") }
    var parentsContactNumber by remember { mutableStateOf(initialStudent?.parentsContactNumber ?: "") }
    var monthlyFeeStr by remember {
        mutableStateOf(
            if (initialStudent != null) initialStudent.monthlyFee.toString() else "2500"
        )
    }
    var markCurrentMonthPaid by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var parentContactError by remember { mutableStateOf<String?>(null) }
    var feeError by remember { mutableStateOf<String?>(null) }

    val isEditing = initialStudent != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Student Details" else "Register Student",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEditing) "Update info and QR code" else "Generate instant QR code pass",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_register_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Student Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Student Name *") },
                    placeholder = { Text("e.g. John Doe") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Student Contact Number
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Student Contact Number") },
                    placeholder = { Text("e.g. +1 (555) 123-4567") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_contact_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Parent's Contact Number
                OutlinedTextField(
                    value = parentsContactNumber,
                    onValueChange = {
                        parentsContactNumber = it
                        if (it.isNotBlank()) parentContactError = null
                    },
                    label = { Text("Parents Contact Number *") },
                    placeholder = { Text("e.g. +1 (555) 987-6543 (for WhatsApp)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    isError = parentContactError != null,
                    supportingText = parentContactError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_contact_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Monthly Fee
                OutlinedTextField(
                    value = monthlyFeeStr,
                    onValueChange = {
                        monthlyFeeStr = it
                        if (it.toDoubleOrNull() != null) feeError = null
                    },
                    label = { Text("Monthly Fee ($currencySymbol) *") },
                    placeholder = { Text("e.g. 2500.00") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    isError = feeError != null,
                    supportingText = feeError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_fee_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Address (optional)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (Optional)") },
                    placeholder = { Text("e.g. 123 Main St, City") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    singleLine = false,
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_address_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox: Paid initial fee
                if (!isEditing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = markCurrentMonthPaid,
                            onCheckedChange = { markCurrentMonthPaid = it },
                            modifier = Modifier.testTag("mark_fee_paid_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark first month's fee as PAID now",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        var hasError = false
                        if (name.isBlank()) {
                            nameError = "Student name is required"
                            hasError = true
                        }
                        if (parentsContactNumber.isBlank()) {
                            parentContactError = "Parent contact number is required"
                            hasError = true
                        }
                        val feeVal = monthlyFeeStr.toDoubleOrNull()
                        if (feeVal == null || feeVal < 0) {
                            feeError = "Please enter a valid monthly fee amount"
                            hasError = true
                        }

                        if (!hasError) {
                            onSave(
                                name,
                                contactNumber,
                                address,
                                parentsContactNumber,
                                feeVal ?: 0.0,
                                markCurrentMonthPaid
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_register_student_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Save Changes" else "Register & Generate QR Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
