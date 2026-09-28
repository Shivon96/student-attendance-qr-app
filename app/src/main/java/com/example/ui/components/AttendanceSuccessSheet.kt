package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Student
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.FeeStatus
import com.example.util.DateUtils
import com.example.util.ShareUtil

@Composable
fun AttendanceSuccessSheet(
    student: Student,
    isAlreadyMarked: Boolean,
    checkInTime: String,
    monthlyAttendedDays: Int,
    totalPaidThisMonth: Double,
    feeStatus: FeeStatus,
    currencySymbol: String = "Rs. ",
    onMarkFeePaid: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentMonthName = DateUtils.formatDisplayMonthYear(DateUtils.getCurrentMonthYearString())

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
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status icon circle
                val iconBg = if (isAlreadyMarked) MaterialTheme.colorScheme.primaryContainer else EmeraldGreen.copy(alpha = 0.15f)
                val iconTint = if (isAlreadyMarked) MaterialTheme.colorScheme.primary else EmeraldGreen

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAlreadyMarked) Icons.Default.Info else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Banner
                Text(
                    text = if (isAlreadyMarked) "Already Checked In Today" else "Attendance Marked!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAlreadyMarked) MaterialTheme.colorScheme.onSurface else EmeraldGreen,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Time: $checkInTime • ${DateUtils.formatDisplayDate(DateUtils.getTodayDateString())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Student Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = student.studentCode,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Row: Attendance & Fees
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Monthly Attendance Stat
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$currentMonthName Attendance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$monthlyAttendedDays Class${if (monthlyAttendedDays == 1) "" else "es"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Fee Status Pill
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Monthly Fee ($currencySymbol${String.format(java.util.Locale.US, "%,.2f", student.monthlyFee)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val (statusText, statusBg, statusColor) = when (feeStatus) {
                                    FeeStatus.PAID -> Triple("PAID", EmeraldGreen.copy(alpha = 0.15f), EmeraldGreen)
                                    FeeStatus.PARTIAL -> Triple("PARTIAL ($currencySymbol${String.format(java.util.Locale.US, "%,.2f", totalPaidThisMonth)})", AmberWarning.copy(alpha = 0.15f), AmberWarning)
                                    FeeStatus.UNPAID -> Triple("PENDING ($currencySymbol${String.format(java.util.Locale.US, "%,.2f", student.monthlyFee)})", RoseError.copy(alpha = 0.15f), RoseError)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusBg,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                // If Fee is pending, show 1-tap "Collect / Mark Fee as Paid"
                if (feeStatus != FeeStatus.PAID && student.monthlyFee > 0) {
                    val remainingFee = student.monthlyFee - totalPaidThisMonth
                    Button(
                        onClick = {
                            onMarkFeePaid(remainingFee)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("collect_fee_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberWarning
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Collect Monthly Fee ($currencySymbol${String.format(java.util.Locale.US, "%,.2f", remainingFee)})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Notify Parent: SMS or WhatsApp
                if (student.parentsContactNumber.isNotBlank()) {
                    val feeNote = if (feeStatus == FeeStatus.PAID) "Monthly fee: Paid." else "Monthly fee: Pending ($currencySymbol${String.format(java.util.Locale.US, "%,.2f", student.monthlyFee)})."
                    val notifyMsg = "Dear Parent,\n${student.name} attended class today at $checkInTime.\n$feeNote\nThank you!"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SMS Option
                        OutlinedButton(
                            onClick = {
                                ShareUtil.sendSmsToNumber(
                                    context = context,
                                    rawPhone = student.parentsContactNumber,
                                    message = notifyMsg
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("sms_parent_attendance_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SMS Parent", fontWeight = FontWeight.SemiBold)
                        }

                        // WhatsApp Option
                        OutlinedButton(
                            onClick = {
                                ShareUtil.sendWhatsAppMessageToNumber(
                                    context = context,
                                    rawPhone = student.parentsContactNumber,
                                    message = notifyMsg
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("whatsapp_parent_attendance_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", color = WhatsAppGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Close / Scan Next button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_attendance_sheet"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Done / Scan Next Student", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
