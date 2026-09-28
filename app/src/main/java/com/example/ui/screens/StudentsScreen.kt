package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.FeeStatus
import com.example.ui.viewmodel.StudentAttendanceFeeStatus
import com.example.ui.viewmodel.StudentViewModel
import com.example.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: StudentViewModel,
    onOpenRegisterDialog: () -> Unit
) {
    val context = LocalContext.current
    val studentsWithStatus by viewModel.studentsWithStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var studentToPayFee by remember { mutableStateOf<Student?>(null) }

    // Edit Dialog
    if (studentToEdit != null) {
        RegisterStudentDialog(
            initialStudent = studentToEdit,
            currencySymbol = currencySymbol,
            onDismiss = { studentToEdit = null },
            onSave = { name, contact, address, parentContact, fee, _ ->
                studentToEdit?.let { existing ->
                    viewModel.updateStudent(
                        existing.copy(
                            name = name,
                            contactNumber = contact,
                            address = address,
                            parentsContactNumber = parentContact,
                            monthlyFee = fee
                        )
                    )
                }
                studentToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (studentToDelete != null) {
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student") },
            text = { Text("Are you sure you want to delete ${studentToDelete?.name}? All associated attendance and fee records will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        studentToDelete?.let { viewModel.deleteStudent(it) }
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_student")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Quick Record Fee Payment Dialog
    if (studentToPayFee != null) {
        val s = studentToPayFee!!
        var paymentAmountStr by remember { mutableStateOf(s.monthlyFee.toString()) }
        var paymentMethod by remember { mutableStateOf("Cash") }

        AlertDialog(
            onDismissRequest = { studentToPayFee = null },
            title = { Text("Record Fee Payment") },
            text = {
                Column {
                    Text(
                        text = "Student: ${s.name} (${s.studentCode})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Monthly Fee: $currencySymbol${String.format(java.util.Locale.US, "%,.2f", s.monthlyFee)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = paymentAmountStr,
                        onValueChange = { paymentAmountStr = it },
                        label = { Text("Amount Paid ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cash", "Online", "Bank").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountStr.toDoubleOrNull() ?: s.monthlyFee
                        viewModel.recordFeePayment(
                            studentId = s.id,
                            monthYear = viewModel.selectedMonthYear.value,
                            amount = amount,
                            paymentMethod = paymentMethod,
                            note = "Manual fee recording"
                        )
                        studentToPayFee = null
                    },
                    modifier = Modifier.testTag("submit_fee_payment")
                ) {
                    Text("Record Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToPayFee = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name, code, or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_students_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips: All, Fee Paid, Fee Pending
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterStatus == null,
                        onClick = { viewModel.setFilterStatus(null) },
                        label = { Text("All (${studentsWithStatus.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FeeStatus.PAID,
                        onClick = { viewModel.setFilterStatus(if (filterStatus == FeeStatus.PAID) null else FeeStatus.PAID) },
                        label = { Text("Fee Paid") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FeeStatus.UNPAID,
                        onClick = { viewModel.setFilterStatus(if (filterStatus == FeeStatus.UNPAID) null else FeeStatus.UNPAID) },
                        label = { Text("Fee Pending") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Students List
            if (studentsWithStatus.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching students found" else "No students registered yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the + button below to register a student and generate their QR pass.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(studentsWithStatus, key = { it.student.id }) { item ->
                        StudentCardItem(
                            item = item,
                            currencySymbol = currencySymbol,
                            onShowQr = { viewModel.setSelectedStudentForQr(item.student) },
                            onMarkAttendance = { viewModel.markAttendance(item.student.id) },
                            onRemoveAttendance = { viewModel.removeAttendanceToday(item.student.id) },
                            onRecordFee = { studentToPayFee = item.student },
                            onEdit = { studentToEdit = item.student },
                            onDelete = { studentToDelete = item.student }
                        )
                    }
                }
            }
        }

        // Floating Action Button: Register Student
        FloatingActionButton(
            onClick = onOpenRegisterDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("register_student_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Register Student")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Student", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StudentCardItem(
    item: StudentAttendanceFeeStatus,
    currencySymbol: String,
    onShowQr: () -> Unit,
    onMarkAttendance: () -> Unit,
    onRemoveAttendance: () -> Unit,
    onRecordFee: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    val student = item.student

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Avatar, Name, Code, More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with initial
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.studentCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (student.parentsContactNumber.isNotBlank()) {
                            Text(" • Parent: ${student.parentsContactNumber}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Fee Status Pill
                val (feeLabel, feeBg, feeColor) = when (item.feeStatus) {
                    FeeStatus.PAID -> Triple("Fee Paid", EmeraldGreen.copy(alpha = 0.15f), EmeraldGreen)
                    FeeStatus.PARTIAL -> Triple("Partial", AmberWarning.copy(alpha = 0.15f), AmberWarning)
                    FeeStatus.UNPAID -> Triple("Pending", RoseError.copy(alpha = 0.15f), RoseError)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = feeBg,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = feeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = feeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View / Print QR Pass") },
                            onClick = {
                                menuExpanded = false
                                onShowQr()
                            },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Record Fee Payment") },
                            onClick = {
                                menuExpanded = false
                                onRecordFee()
                            },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Student") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Student", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Sub info: Today Attendance & Monthly Attendance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.isPresentToday) Icons.Default.CheckCircle else Icons.Outlined.Event,
                        contentDescription = null,
                        tint = if (item.isPresentToday) EmeraldGreen else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.isPresentToday) "Attended today (${item.todayCheckInTime ?: "Yes"})" else "Not yet attended today",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isPresentToday) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${item.monthlyAttendedDays} classes this month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar on Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View QR Button
                Button(
                    onClick = onShowQr,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("show_qr_card_btn_${student.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("QR Pass", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // WhatsApp Parent Button
                if (student.parentsContactNumber.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val feeTxt = if (item.feeStatus == FeeStatus.PAID) "Paid" else "Pending ($currencySymbol${String.format(java.util.Locale.US, "%,.2f", student.monthlyFee)})"
                            val msg = "Dear Parent, this is an update regarding ${student.name}. Monthly Fee: $feeTxt."
                            ShareUtil.sendWhatsAppMessageToNumber(context, student.parentsContactNumber, msg)
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("whatsapp_parent_btn_${student.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 13.sp, color = WhatsAppGreen)
                    }
                }

                // 1-tap Attendance Toggle
                if (item.isPresentToday) {
                    OutlinedButton(
                        onClick = onRemoveAttendance,
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Undo Today", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Button(
                        onClick = onMarkAttendance,
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check In", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
