package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CloudBackupDialog
import com.example.ui.components.PaymentRecordedDialog
import com.example.ui.viewmodel.StudentViewModel

sealed class AppTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Scanner : AppTab("Scan QR", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    object Students : AppTab("Students", Icons.Filled.People, Icons.Outlined.People)
    object Fees : AppTab("Fees", Icons.Filled.Payments, Icons.Outlined.Payments)
    object History : AppTab("History", Icons.Filled.EventNote, Icons.Outlined.EventNote)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: StudentViewModel) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showCloudBackupDialog by remember { mutableStateOf(false) }

    val selectedStudentForQr by viewModel.selectedStudentForQr.collectAsState()
    val lastPaymentReceipt by viewModel.lastPaymentReceipt.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allAttendance by viewModel.allAttendanceRecords.collectAsState()
    val allPayments by viewModel.allFeePayments.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val tabs = listOf(
        AppTab.Scanner,
        AppTab.Students,
        AppTab.Fees,
        AppTab.History
    )

    // Payment Confirmation & Parent SMS Prompt Dialog
    lastPaymentReceipt?.let { receipt ->
        PaymentRecordedDialog(
            receipt = receipt,
            currencySymbol = currencySymbol,
            onDismiss = { viewModel.clearLastPaymentReceipt() }
        )
    }

    // Google Drive & Sheets Cloud Backup Dialog
    if (showCloudBackupDialog) {
        CloudBackupDialog(
            students = allStudents,
            attendance = allAttendance,
            payments = allPayments,
            currencySymbol = currencySymbol,
            onDismiss = { showCloudBackupDialog = false }
        )
    }

    // QR Card Dialog (triggered when user clicks View QR or registers a student)
    selectedStudentForQr?.let { student ->
        StudentQrCardDialog(
            student = student,
            currencySymbol = currencySymbol,
            onDismiss = { viewModel.setSelectedStudentForQr(null) }
        )
    }

    // Register Student Dialog
    if (showRegisterDialog) {
        RegisterStudentDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showRegisterDialog = false },
            onSave = { name, contact, address, parentContact, fee, markPaid ->
                viewModel.registerStudent(
                    name = name,
                    contactNumber = contact,
                    address = address,
                    parentsContactNumber = parentContact,
                    monthlyFee = fee,
                    markCurrentMonthPaid = markPaid,
                    onRegistered = {
                        showRegisterDialog = false
                    }
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "StudentQR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Google Drive & Sheets Backup Action
                    IconButton(
                        onClick = { showCloudBackupDialog = true },
                        modifier = Modifier.testTag("appbar_cloud_backup_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Google Drive / Sheets Backup",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Register Student Action
                    IconButton(
                        onClick = { showRegisterDialog = true },
                        modifier = Modifier.testTag("appbar_register_student_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Register Student",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.title.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> ScannerScreen(
                    viewModel = viewModel,
                    onNavigateToStudents = { selectedTabIndex = 1 }
                )
                1 -> StudentsScreen(
                    viewModel = viewModel,
                    onOpenRegisterDialog = { showRegisterDialog = true }
                )
                2 -> FeeManagementScreen(
                    viewModel = viewModel
                )
                3 -> AttendanceHistoryScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
