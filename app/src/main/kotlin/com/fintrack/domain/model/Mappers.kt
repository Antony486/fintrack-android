package com.fintrack.domain.model

import com.fintrack.data.local.entities.BillEntity
import com.fintrack.data.local.entities.BudgetEntity
import com.fintrack.data.local.entities.TransactionEntity
import com.fintrack.data.remote.dto.*

// DTO → Domain
fun TransactionDto.toDomain() = Transaction(
    id = id, userId = userId, title = title, amount = amount, type = type,
    category = category, currency = currency, amountKes = amountKes, notes = notes,
    date = date, paymentMethod = paymentMethod, createdAt = createdAt, updatedAt = updatedAt
)

fun BudgetDto.toDomain() = Budget(
    id = id, userId = userId, category = category, limitAmount = limitAmount,
    currency = currency, month = month, year = year, createdAt = createdAt, updatedAt = updatedAt
)

fun BudgetStatusDto.toDomain() = BudgetStatus(
    budget = budget.toDomain(), spent = spent, remaining = remaining, percentage = percentage
)

fun BillDto.toDomain() = Bill(
    id = id, userId = userId, title = title, amount = amount, currency = currency,
    recurrence = recurrence, dueDay = dueDay, nextDueDate = nextDueDate, isPaid = isPaid,
    category = category, notes = notes, createdAt = createdAt, updatedAt = updatedAt
)

fun AuthResponseDto.toDomain() = User(
    token = token, userId = userId, username = username, email = email
)

fun TransactionSummaryDto.toDomain() = TransactionSummary(
    totalIncome = totalIncome, totalExpense = totalExpense, netBalance = netBalance,
    byCategory = byCategory.map { CategorySummary(it.category, it.total) }
)

// Domain → Entity
fun Transaction.toEntity() = TransactionEntity(
    id = id, userId = userId, title = title, amount = amount, type = type,
    category = category, currency = currency, amountKes = amountKes, notes = notes,
    date = date, paymentMethod = paymentMethod, createdAt = createdAt, updatedAt = updatedAt
)

fun Budget.toEntity() = BudgetEntity(
    id = id, userId = userId, category = category, limitAmount = limitAmount,
    currency = currency, month = month, year = year, createdAt = createdAt, updatedAt = updatedAt
)

fun Bill.toEntity() = BillEntity(
    id = id, userId = userId, title = title, amount = amount, currency = currency,
    recurrence = recurrence, dueDay = dueDay, nextDueDate = nextDueDate, isPaid = isPaid,
    category = category, notes = notes, createdAt = createdAt, updatedAt = updatedAt
)

// Entity → Domain
fun TransactionEntity.toDomain() = Transaction(
    id = id, userId = userId, title = title, amount = amount, type = type,
    category = category, currency = currency, amountKes = amountKes, notes = notes,
    date = date, paymentMethod = paymentMethod, createdAt = createdAt, updatedAt = updatedAt
)

fun BudgetEntity.toDomain() = Budget(
    id = id, userId = userId, category = category, limitAmount = limitAmount,
    currency = currency, month = month, year = year, createdAt = createdAt, updatedAt = updatedAt
)

fun BillEntity.toDomain() = Bill(
    id = id, userId = userId, title = title, amount = amount, currency = currency,
    recurrence = recurrence, dueDay = dueDay, nextDueDate = nextDueDate, isPaid = isPaid,
    category = category, notes = notes, createdAt = createdAt, updatedAt = updatedAt
)
