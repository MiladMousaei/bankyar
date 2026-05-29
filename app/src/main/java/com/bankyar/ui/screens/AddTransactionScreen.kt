package com.bankyar.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bankyar.data.database.entities.Transaction
import com.bankyar.data.database.entities.TransactionCategory
import com.bankyar.data.database.entities.TransactionType
import com.bankyar.ui.components.formatAmount
import com.bankyar.ui.theme.*
import com.bankyar.ui.viewmodels.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    userId: Int,
    editId: Int = -1,
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    var existing by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(editId) {
        if (editId > 0) existing = viewModel.getById(editId)
    }

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(TransactionCategory.OTHER) }
    var description by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("حساب اصلی") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existing) {
        existing?.let { t ->
            title = t.title
            amountText = t.amount.toLong().toString()
            type = t.type
            category = t.category
            description = t.description
            accountName = t.accountName
        }
    }

    val isEdit = editId > 0
    val typeColor = when (type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "ویرایش تراکنش" else "تراکنش جدید", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector
            SectionCard("نوع تراکنش") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.values().forEach { t ->
                        val (label, color, bg) = when (t) {
                            TransactionType.INCOME -> Triple("درآمد", IncomeGreen, IncomeGreenLight)
                            TransactionType.EXPENSE -> Triple("هزینه", ExpenseRed, ExpenseRedLight)
                            TransactionType.TRANSFER -> Triple("انتقال", TransferBlue, TransferBlueLight)
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (type == t) color else bg)
                                .clickable { type = t; if (t == TransactionType.TRANSFER) category = TransactionCategory.TRANSFER }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (type == t) Color.White else color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Amount
            SectionCard("مبلغ") {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("مبلغ (تومان)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = typeColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = typeColor, unfocusedBorderColor = TextHint),
                    suffix = { Text("تومان", color = TextSecondary) }
                )
                if (amountText.isNotBlank()) {
                    Text(
                        "مبلغ: ${formatAmount(amountText.toDoubleOrNull() ?: 0.0)} تومان",
                        color = typeColor, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Title
            SectionCard("عنوان") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان تراکنش") },
                    leadingIcon = { Icon(Icons.Default.Title, null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = TextHint)
                )
            }

            // Category
            SectionCard("دسته‌بندی") {
                val cats = if (type == TransactionType.INCOME)
                    listOf(TransactionCategory.SALARY, TransactionCategory.INVESTMENT, TransactionCategory.OTHER)
                else if (type == TransactionType.TRANSFER)
                    listOf(TransactionCategory.TRANSFER)
                else TransactionCategory.values().filter { it != TransactionCategory.SALARY && it != TransactionCategory.TRANSFER }.toList()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cats.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { cat ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (category == cat) Primary.copy(0.15f) else SurfaceVariant)
                                        .border(
                                            1.dp,
                                            if (category == cat) Primary else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { category = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(cat.icon, fontSize = 18.sp)
                                        Text(cat.label, fontSize = 10.sp, color = if (category == cat) Primary else TextSecondary)
                                    }
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // Account
            SectionCard("نام حساب") {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("حساب بانکی") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = TextHint)
                )
            }

            // Description
            SectionCard("توضیحات") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات (اختیاری)") },
                    leadingIcon = { Icon(Icons.Default.Notes, null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = TextHint)
                )
            }

            // Error
            error?.let {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ExpenseRedLight).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = ExpenseRed, fontSize = 13.sp)
                }
            }

            // Save button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        title.isBlank() -> error = "عنوان را وارد کنید"
                        amount == null || amount <= 0 -> error = "مبلغ معتبر وارد کنید"
                        else -> {
                            error = null
                            val t = Transaction(
                                id = if (isEdit) editId else 0,
                                userId = userId,
                                title = title,
                                amount = amount,
                                type = type,
                                category = category,
                                description = description,
                                accountName = accountName
                            )
                            if (isEdit) viewModel.updateTransaction(t) else viewModel.addTransaction(t)
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = typeColor)
            ) {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) "ذخیره تغییرات" else "ثبت تراکنش", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            content()
        }
    }
}
