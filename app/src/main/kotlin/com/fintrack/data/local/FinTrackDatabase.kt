package com.fintrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fintrack.data.local.dao.BillDao
import com.fintrack.data.local.dao.BudgetDao
import com.fintrack.data.local.dao.TransactionDao
import com.fintrack.data.local.entities.BillEntity
import com.fintrack.data.local.entities.BudgetEntity
import com.fintrack.data.local.entities.TransactionEntity

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, BillEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun billDao(): BillDao
}
