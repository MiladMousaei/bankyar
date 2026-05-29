package com.bankyar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bankyar.data.database.entities.Transaction
import com.bankyar.data.database.entities.TransactionType
import com.bankyar.ui.components.formatAmount
import com.bankyar.ui.theme.*
import com.bankyar.ui.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    viewModel: TransactionViewModel,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    var transaction by remember { mutableStateOf<Transaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        transaction = viewModel.getById(transactionId)
    }

    val t = transaction ?: return

    val (typeBg, typeFg, typeLabel) = when (t.type) {
        TransactionType.INCOME -> Triple(IncomeGreenLight, IncomeGreen, "درآمد")
        TransactionType.EXPENSE -> Triple(ExpenseRedLight, ExpenseRed, "هزینه")
        TransactionType.TRANSFER -> Triple(TransferBlueLight, TransferBlue, "انتقال")
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف تراکنش") },
            text = { Text("آیا از حذف این تراکنش اطمینان دارید؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(t)
                    showDeleteDialog = false
                    onBack()
                }) { Text("حذف", color = ExpenseRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton({ showDeleteDialog = false }) { Text("انصراف") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات تراکنش", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton({ onEdit(t.id) }) { Icon(Icons.Default.Edit, null, tint = Color.White) }
                    IconButton({ showDeleteDialog = true }) { Icon(Icons.Default.Delete, null, tint = Color.White.copy(0.9f)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(Background).padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = typeBg),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(70.dp).clip(CircleShape).background(typeFg.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Text(t.category.icon, fontSize = 32.sp) }
                    Spacer(Modifier.height(12.dp))
                    Text(t.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(typeFg.copy(0.15f)).padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(typeFg))
                        Spacer(Modifier.width(6.dp))
                        Text(typeLabel, color = typeFg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(16.dp))
                    val prefix = when (t.type) { TransactionType.INCOME -> "+"; TransactionType.EXPENSE -> "-"; else -> "" }
                    Text("$prefix${formatAmount(t.amount)} تومان", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = typeFg)
                }
            }

            // Details
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRow(Icons.Default.Category, "دسته‌بندی", "${t.category.icon} ${t.category.label}")
                    Divider(color = SurfaceVariant)
                    DetailRow(Icons.Default.AccountBalance, "حساب", t.accountName)
                    Divider(color = SurfaceVariant)
                    DetailRow(Icons.Default.CalendarToday, "تاریخ",
                        SimpleDateFormat("EEEE، dd MMMM yyyy - HH:mm", Locale.getDefault()).format(Date(t.date)))
                    if (t.description.isNotBlank()) {
                        Divider(color = SurfaceVariant)
                        DetailRow(Icons.Default.Notes, "توضیحات", t.description)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}
