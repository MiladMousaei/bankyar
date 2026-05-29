package com.bankyar.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.bankyar.ui.components.*
import com.bankyar.ui.theme.*
import com.bankyar.ui.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    userName: String,
    viewModel: TransactionViewModel,
    onAddTransaction: () -> Unit,
    onViewAll: () -> Unit,
    onTransactionClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(userId) { viewModel.setUser(userId) }

    val stats by viewModel.stats.collectAsState()
    val recent by viewModel.recentTransactions.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                containerColor = Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("تراکنش جدید", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().background(Background).padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item { TopBar(userName, onLogout) }
            item { BalanceCard(stats.totalBalance, stats.totalIncome, stats.totalExpense) }
            item { QuickActions(onAddTransaction) }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("تراکنش‌های اخیر")
                    TextButton(onClick = onViewAll) {
                        Text("مشاهده همه", color = Primary, fontSize = 13.sp)
                    }
                }
            }
            if (recent.isEmpty()) {
                item { EmptyState() }
            } else {
                items(recent) { t ->
                    TransactionItem(t, onClick = { onTransactionClick(t.id) })
                }
            }
        }
    }
}

@Composable
private fun TopBar(userName: String, onLogout: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Primary).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("خوش آمدید،", color = Color.White.copy(0.8f), fontSize = 13.sp)
            Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(userName.first().toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onLogout) {
                Icon(Icons.Default.Logout, null, tint = Color.White.copy(0.8f))
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Double, income: Double, expense: Double) {
    GradientCard(Modifier.fillMaxWidth().padding(20.dp)) {
        Column {
            Text("موجودی کل", color = Color.White.copy(0.8f), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "${formatAmount(balance)} تومان",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatChip("درآمد", income, IncomeGreen)
                StatChip("هزینه", expense, ExpenseRed)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, amount: Double, color: Color) {
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(0.15f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(0.8f), fontSize = 11.sp)
            Text("${formatAmount(amount)} ت", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun QuickActions(onAdd: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionBtn(Modifier.weight(1f), Icons.Default.AddCircle, "درآمد", IncomeGreenLight, IncomeGreen, onAdd)
        QuickActionBtn(Modifier.weight(1f), Icons.Default.RemoveCircle, "هزینه", ExpenseRedLight, ExpenseRed, onAdd)
        QuickActionBtn(Modifier.weight(1f), Icons.Default.SwapHoriz, "انتقال", TransferBlueLight, TransferBlue, onAdd)
    }
}

@Composable
private fun QuickActionBtn(
    modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, bg: Color, fg: Color, onClick: () -> Unit
) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TransactionItem(t: Transaction, onClick: () -> Unit) {
    val (bg, fg) = when (t.type) {
        TransactionType.INCOME -> IncomeGreenLight to IncomeGreen
        TransactionType.EXPENSE -> ExpenseRedLight to ExpenseRed
        TransactionType.TRANSFER -> TransferBlueLight to TransferBlue
    }
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(t.category.icon, fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(t.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                Text(t.category.label, color = TextSecondary, fontSize = 12.sp)
                if (t.description.isNotBlank()) {
                    Text(t.description, color = TextHint, fontSize = 11.sp, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                AmountText(t.amount, t.type, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(t.date)),
                    color = TextHint, fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AccountBalanceWallet, null, tint = TextHint, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text("هنوز تراکنشی ثبت نشده", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text("اولین تراکنش خود را اضافه کنید", color = TextHint, fontSize = 13.sp)
    }
}
