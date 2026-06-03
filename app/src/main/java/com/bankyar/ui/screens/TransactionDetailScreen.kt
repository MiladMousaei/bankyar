package com.bankyar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bankyar.data.database.entities.Transaction
import com.bankyar.data.database.entities.TransactionType
import com.bankyar.ui.components.formatAmount
import com.bankyar.ui.viewmodels.TransactionViewModel
import com.bankyar.util.JalaliCalendar

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

    LaunchedEffect(transactionId) { transaction = viewModel.getById(transactionId) }

    val t = transaction ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val (typeLabel, typeFg, typeBg) = when (t.type) {
        TransactionType.INCOME -> Triple("درآمد", Color(0xFF2E7D32), Color(0xFFE8F5E9))
        TransactionType.EXPENSE -> Triple("هزینه", Color(0xFFC62828), Color(0xFFFFEBEE))
        TransactionType.TRANSFER -> Triple("انتقال", Color(0xFF1565C0), Color(0xFFE3F2FD))
    }
    val prefix = when (t.type) { TransactionType.INCOME -> "+"; TransactionType.EXPENSE -> "-"; else -> "" }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف تراکنش", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("آیا از حذف این تراکنش اطمینان دارید؟", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton({
                    viewModel.deleteTransaction(t); showDeleteDialog = false; onBack()
                }) { Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton({ showDeleteDialog = false }) { Text("انصراف") } }
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(70.dp).clip(CircleShape).background(typeBg),
                        contentAlignment = Alignment.Center
                    ) { Text(t.category.icon, fontSize = 32.sp) }

                    Spacer(Modifier.height(12.dp))
                    Text(t.title, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))

                    Row(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(typeBg)
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(typeFg))
                        Spacer(Modifier.width(6.dp))
                        Text(typeLabel, color = typeFg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("$prefix${formatAmount(t.amount)} تومان",
                        fontSize = 26.sp, fontWeight = FontWeight.Bold, color = typeFg)
                }
            }

            // Details card
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DetailRow(Icons.Default.Category, "دسته‌بندی",
                        "${t.category.icon} ${t.category.label}")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                    DetailRow(Icons.Default.AccountBalance, "حساب", t.accountName)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                    DetailRow(Icons.Default.CalendarToday, "تاریخ (شمسی)",
                        JalaliCalendar.toJalaliString(t.date))
                    if (t.description.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                        DetailRow(Icons.Default.Notes, "توضیحات", t.description)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}
