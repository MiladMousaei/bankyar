package com.bankyar.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bankyar.data.database.entities.TransactionType
import com.bankyar.ui.theme.*
import com.bankyar.ui.viewmodels.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit,
    onAddTransaction: () -> Unit,
    onTransactionClick: (Int) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var filterType by remember { mutableStateOf<TransactionType?>(null) }

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    val filtered = if (filterType == null) transactions else transactions.filter { it.type == filterType }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("همه تراکنش‌ها", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction, containerColor = Primary, contentColor = Color.White) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(Background).padding(padding)) {
            // Search
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("جستجو در تراکنش‌ها...", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                trailingIcon = {
                    if (query.isNotBlank()) IconButton({ viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, null, tint = TextSecondary)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            // Filter chips
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(null, "همه", filterType, { filterType = it })
                FilterChip(TransactionType.INCOME, "درآمد", filterType, { filterType = it })
                FilterChip(TransactionType.EXPENSE, "هزینه", filterType, { filterType = it })
                FilterChip(TransactionType.TRANSFER, "انتقال", filterType, { filterType = it })
            }

            Spacer(Modifier.height(8.dp))

            // Count
            Text(
                "${filtered.size} تراکنش",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, tint = TextHint, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("نتیجه‌ای یافت نشد", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(filtered, key = { it.id }) { t ->
                        TransactionItem(t, onClick = { onTransactionClick(t.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    type: TransactionType?,
    label: String,
    selected: TransactionType?,
    onClick: (TransactionType?) -> Unit
) {
    val isSelected = selected == type
    val (bg, fg) = when {
        !isSelected -> SurfaceVariant to TextSecondary
        type == TransactionType.INCOME -> IncomeGreenLight to IncomeGreen
        type == TransactionType.EXPENSE -> ExpenseRedLight to ExpenseRed
        type == TransactionType.TRANSFER -> TransferBlueLight to TransferBlue
        else -> Primary.copy(0.15f) to Primary
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick(type) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = fg, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
