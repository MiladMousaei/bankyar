package com.bankyar.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bankyar.data.database.entities.Transaction
import com.bankyar.data.database.entities.TransactionType
import com.bankyar.ui.components.formatAmount
import com.bankyar.ui.theme.*
import com.bankyar.ui.viewmodels.TransactionViewModel
import com.bankyar.util.JalaliCalendar
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    userId: Int,
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(userId) { viewModel.setUser(userId) }

    val transactions by viewModel.transactions.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showFormatDialog by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("csv") }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                stream.write(buildCsv(transactions).toByteArray(Charsets.UTF_8))
            }
        }
    }

    val txtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                stream.write(buildTxt(transactions, stats.totalBalance, stats.totalIncome, stats.totalExpense).toByteArray(Charsets.UTF_8))
            }
        }
    }

    if (showFormatDialog) {
        AlertDialog(
            onDismissRequest = { showFormatDialog = false },
            title = { Text("انتخاب فرمت خروجی", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("csv" to "CSV (اکسل)", "txt" to "TXT (متنی)").forEach { (fmt, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()) {
                            RadioButton(selected = selectedFormat == fmt,
                                onClick = { selectedFormat = fmt })
                            Text(label, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showFormatDialog = false
                    val fileName = "bankyar_report_${System.currentTimeMillis()}"
                    if (selectedFormat == "csv") csvLauncher.launch("$fileName.csv")
                    else txtLauncher.launch("$fileName.txt")
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("دانلود", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton({ showFormatDialog = false }) { Text("انصراف") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("گزارشات", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton({ showFormatDialog = true }) {
                        Icon(Icons.Default.FileDownload, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary card
            item {
                Card(
                    Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("خلاصه مالی", color = Color.White.copy(0.8f), fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("${formatAmount(stats.totalBalance)} تومان",
                            color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MiniStat("درآمد کل", stats.totalIncome, Color(0xFF81C784))
                            MiniStat("هزینه کل", stats.totalExpense, Color(0xFFEF9A9A))
                            MiniStat("تعداد", transactions.size.toDouble(), Color.White, isCount = true)
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("همه تراکنش‌ها (${transactions.size})",
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    TextButton({ showFormatDialog = true }) {
                        Icon(Icons.Default.FileDownload, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("دریافت فایل", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("هیچ تراکنشی یافت نشد", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(transactions, key = { it.id }) { t ->
                    TransactionItem(t, onClick = {})
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Double, color: Color, isCount: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(0.7f), fontSize = 11.sp)
        Text(
            if (isCount) value.toInt().toString() else "${formatAmount(value)} ت",
            color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp
        )
    }
}

private fun buildCsv(list: List<Transaction>): String {
    val sb = StringBuilder()
    sb.appendLine("تاریخ,عنوان,مبلغ,نوع,دسته‌بندی,حساب,توضیحات")
    list.forEach { t ->
        val type = when (t.type) { TransactionType.INCOME -> "درآمد"; TransactionType.EXPENSE -> "هزینه"; TransactionType.TRANSFER -> "انتقال" }
        val date = JalaliCalendar.toJalaliShort(t.date)
        sb.appendLine("$date,\"${t.title}\",${t.amount},$type,${t.category.label},\"${t.accountName}\",\"${t.description}\"")
    }
    return sb.toString()
}

private fun buildTxt(list: List<Transaction>, balance: Double, income: Double, expense: Double): String {
    val sb = StringBuilder()
    sb.appendLine("═══════════════════════════════════════════")
    sb.appendLine("          بانک‌یار - گزارش تراکنش‌ها")
    sb.appendLine("═══════════════════════════════════════════")
    sb.appendLine()
    sb.appendLine("موجودی کل:  ${formatAmount(balance)} تومان")
    sb.appendLine("درآمد کل:   ${formatAmount(income)} تومان")
    sb.appendLine("هزینه کل:   ${formatAmount(expense)} تومان")
    sb.appendLine("تعداد تراکنش: ${list.size}")
    sb.appendLine()
    sb.appendLine("───────────────────────────────────────────")
    list.forEach { t ->
        val type = when (t.type) { TransactionType.INCOME -> "درآمد ↑"; TransactionType.EXPENSE -> "هزینه ↓"; TransactionType.TRANSFER -> "انتقال ↔" }
        val prefix = when (t.type) { TransactionType.INCOME -> "+"; TransactionType.EXPENSE -> "-"; else -> "" }
        sb.appendLine("📅 ${JalaliCalendar.toJalaliString(t.date)}")
        sb.appendLine("📌 ${t.title}  |  $type")
        sb.appendLine("💰 $prefix${formatAmount(t.amount)} تومان")
        sb.appendLine("📂 ${t.category.label}  |  🏦 ${t.accountName}")
        if (t.description.isNotBlank()) sb.appendLine("📝 ${t.description}")
        sb.appendLine("───────────────────────────────────────────")
    }
    return sb.toString()
}
