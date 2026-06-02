package com.fintrack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val currency: String,
    val amountKes: Double,
    val notes: String?,
    val date: String,
    val paymentMethod: String,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val category: String,
    val limitAmount: Double,
    val currency: String,
    val month: Int,
    val year: Int,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val amount: Double,
    val currency: String,
    val recurrence: String,
    val dueDay: Int,
    val nextDueDate: String,
    val isPaid: Boolean,
    val category: String,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)
