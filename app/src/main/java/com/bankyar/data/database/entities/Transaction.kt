package com.bankyar.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { INCOME, EXPENSE, TRANSFER }

enum class TransactionCategory(val label: String, val icon: String) {
    SALARY("حقوق", "💼"),
    FOOD("خوراک", "🍔"),
    TRANSPORT("حمل و نقل", "🚗"),
    SHOPPING("خرید", "🛍️"),
    HEALTH("بهداشت", "🏥"),
    EDUCATION("آموزش", "📚"),
    ENTERTAINMENT("تفریح", "🎮"),
    BILLS("قبوض", "📄"),
    INVESTMENT("سرمایه‌گذاری", "📈"),
    TRANSFER("انتقال", "🔄"),
    OTHER("سایر", "💰")
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val accountName: String = "حساب اصلی"
)
