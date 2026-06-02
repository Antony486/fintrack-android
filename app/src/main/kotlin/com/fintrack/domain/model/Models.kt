package com.fintrack.domain.model

data class Transaction(
    val id: Int,
    val userId: Int,
    val title: String,
    val amount: Double,
    val type: String,         // INCOME or EXPENSE
    val category: String,
    val currency: String,
    val amountKes: Double,
    val notes: String?,
    val date: String,
    val paymentMethod: String,
    val createdAt: String,
    val updatedAt: String
)

data class TransactionSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val byCategory: List<CategorySummary>
)

data class CategorySummary(val category: String, val total: Double)

data class Budget(
    val id: Int,
    val userId: Int,
    val category: String,
    val limitAmount: Double,
    val currency: String,
    val month: Int,
    val year: Int,
    val createdAt: String,
    val updatedAt: String
)

data class BudgetStatus(
    val budget: Budget,
    val spent: Double,
    val remaining: Double,
    val percentage: Double
)

data class Bill(
    val id: Int,
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

data class User(
    val token: String,
    val userId: Int,
    val username: String,
    val email: String
)

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
