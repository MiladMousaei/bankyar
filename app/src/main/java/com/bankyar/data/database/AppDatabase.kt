package com.bankyar.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bankyar.data.database.dao.TransactionDao
import com.bankyar.data.database.dao.UserDao
import com.bankyar.data.database.entities.Transaction
import com.bankyar.data.database.entities.User

@Database(entities = [User::class, Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "bankyar.db")
                    .build().also { INSTANCE = it }
            }
    }
}
