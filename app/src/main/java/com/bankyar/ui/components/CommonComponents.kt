package com.bankyar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bankyar.ui.theme.*
import java.text.DecimalFormat

fun formatAmount(amount: Double): String {
    val df = DecimalFormat("#,###")
    return df.format(amount)
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
            .padding(20.dp),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = modifier
    )
}

@Composable
fun AmountText(
    amount: Double,
    type: com.bankyar.data.database.entities.TransactionType,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    val (color, prefix) = when (type) {
        com.bankyar.data.database.entities.TransactionType.INCOME -> IncomeGreen to "+"
        com.bankyar.data.database.entities.TransactionType.EXPENSE -> ExpenseRed to "-"
        com.bankyar.data.database.entities.TransactionType.TRANSFER -> TransferBlue to ""
    }
    Text(
        text = "$prefix${formatAmount(amount)} تومان",
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun LoadingOverlay() {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Primary)
    }
}
