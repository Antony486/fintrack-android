package com.fintrack.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Auth ────────────────────────────────────────────────────────────────────

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val userId: Int,
    val username: String,
    val email: String
)

// ─── Transaction ─────────────────────────────────────────────────────────────

@Serializable
data class TransactionRequestDto(
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val currency: String,
    val notes: String? = null,
    val date: String,
    val paymentMethod: String
)

@Serializable
data class TransactionUpdateDto(
    val title: String? = null,
    val amount: Double? = null,
    val type: String? = null,
    val category: String? = null,
    val currency: String? = null,
    val notes: String? = null,
    val date: String? = null,
    val paymentMethod: String? = null
)

@Serializable
data class TransactionDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val currency: String,
    val amountKes: Double,
    val notes: String? = null,
    val date: String,
    val paymentMethod: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CategorySummaryDto(val category: String, val total: Double)

@Serializable
data class TransactionSummaryDto(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val byCategory: List<CategorySummaryDto>
)

// ─── Budget ──────────────────────────────────────────────────────────────────

@Serializable
data class BudgetRequestDto(
    val category: String,
    val limitAmount: Double,
    val currency: String,
    val month: Int,
    val year: Int
)

@Serializable
data class BudgetUpdateDto(
    val category: String? = null,
    val limitAmount: Double? = null,
    val currency: String? = null,
    val month: Int? = null,
    val year: Int? = null
)

@Serializable
data class BudgetDto(
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

@Serializable
data class BudgetStatusDto(
    val budget: BudgetDto,
    val spent: Double,
    val remaining: Double,
    val percentage: Double
)

// ─── Bill ────────────────────────────────────────────────────────────────────

@Serializable
data class BillRequestDto(
    val title: String,
    val amount: Double,
    val currency: String,
    val recurrence: String,
    val dueDay: Int,
    val category: String,
    val notes: String? = null
)

@Serializable
data class BillUpdateDto(
    val title: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val recurrence: String? = null,
    val dueDay: Int? = null,
    val category: String? = null,
    val notes: String? = null
)

@Serializable
data class BillDto(
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
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

// ─── Currency ─────────────────────────────────────────────────────────────────

@Serializable
data class RatesResponseDto(
    val base: String,
    val rates: Map<String, Double>
)

@Serializable
data class ConvertResponseDto(
    val from: String,
    val to: String,
    val amount: Double,
    val converted: Double,
    val rate: Double
)

// ─── M-Pesa ──────────────────────────────────────────────────────────────────

@Serializable
data class StkPushRequestDto(
    val phone: String,
    val amount: Double,
    val accountRef: String,
    val description: String
)

@Serializable
data class StkPushResponseDto(
    val checkoutRequestId: String,
    val message: String
)

@Serializable
data class MpesaStatusResponseDto(
    val status: String,
    val amount: Double?,
    val phone: String?
)

// ─── Stripe ──────────────────────────────────────────────────────────────────

@Serializable
data class StripePaymentIntentRequestDto(
    val amount: Double,
    val currency: String = "kes"
)

@Serializable
data class StripePaymentIntentResponseDto(
    val clientSecret: String,
    val paymentIntentId: String
)

// ─── Generic ─────────────────────────────────────────────────────────────────

@Serializable
data class MessageResponseDto(val message: String)

@Serializable
data class ErrorResponseDto(val error: String)
