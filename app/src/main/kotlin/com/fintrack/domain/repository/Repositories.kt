package com.fintrack.domain.repository

import com.fintrack.data.local.SessionManager
import com.fintrack.data.local.dao.BillDao
import com.fintrack.data.local.dao.BudgetDao
import com.fintrack.data.local.dao.TransactionDao
import com.fintrack.data.remote.api.*
import com.fintrack.data.remote.dto.*
import com.fintrack.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─── Auth ────────────────────────────────────────────────────────────────────

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {
    suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            val response = authApi.register(RegisterRequestDto(username, email, password))
            if (response.isSuccessful) {
                val user = response.body()!!.toDomain()
                sessionManager.saveSession(user.token, user.userId, user.username, user.email)
                Result.Success(user)
            } else {
                Result.Error(response.errorBody()?.string() ?: "Registration failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequestDto(email, password))
            if (response.isSuccessful) {
                val user = response.body()!!.toDomain()
                sessionManager.saveSession(user.token, user.userId, user.username, user.email)
                Result.Success(user)
            } else {
                Result.Error("Invalid email or password")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() = sessionManager.clearSession()
    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn
    val username: Flow<String?> = sessionManager.username
    val email: Flow<String?> = sessionManager.email
}

// ─── Transaction ─────────────────────────────────────────────────────────────

class TransactionRepository(
    private val api: TransactionApi,
    private val dao: TransactionDao
) {
    val localTransactions: Flow<List<Transaction>> = dao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun fetchAll(
        type: String? = null,
        category: String? = null,
        currency: String? = null
    ): Result<List<Transaction>> {
        return try {
            val response = api.getAll(type, category, currency)
            if (response.isSuccessful) {
                val transactions = response.body()!!.map { it.toDomain() }
                dao.deleteAll()
                dao.insertAll(transactions.map { it.toEntity() })
                Result.Success(transactions)
            } else {
                Result.Error("Failed to fetch transactions")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSummary(month: Int? = null, year: Int? = null): Result<TransactionSummary> {
        return try {
            val response = api.getSummary(month, year)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.toDomain())
            } else {
                Result.Error("Failed to fetch summary")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun create(
        title: String, amount: Double, type: String, category: String,
        currency: String, notes: String?, date: String, paymentMethod: String
    ): Result<Transaction> {
        return try {
            val response = api.create(
                TransactionRequestDto(title, amount, type, category, currency, notes, date, paymentMethod)
            )
            if (response.isSuccessful) {
                val transaction = response.body()!!.toDomain()
                dao.insert(transaction.toEntity())
                Result.Success(transaction)
            } else {
                Result.Error("Failed to create transaction")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun update(id: Int, request: TransactionUpdateDto): Result<Transaction> {
        return try {
            val response = api.update(id, request)
            if (response.isSuccessful) {
                val transaction = response.body()!!.toDomain()
                dao.insert(transaction.toEntity())
                Result.Success(transaction)
            } else {
                Result.Error("Failed to update transaction")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun delete(id: Int): Result<Unit> {
        return try {
            val response = api.delete(id)
            if (response.isSuccessful) {
                dao.deleteById(id)
                Result.Success(Unit)
            } else {
                Result.Error("Failed to delete transaction")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}

// ─── Budget ──────────────────────────────────────────────────────────────────

class BudgetRepository(
    private val api: BudgetApi,
    private val dao: BudgetDao
) {
    val localBudgets: Flow<List<Budget>> = dao.getAll().map { it.map { e -> e.toDomain() } }

    suspend fun fetchAll(month: Int? = null, year: Int? = null): Result<List<Budget>> {
        return try {
            val response = api.getAll(month, year)
            if (response.isSuccessful) {
                val budgets = response.body()!!.map { it.toDomain() }
                dao.deleteAll()
                dao.insertAll(budgets.map { it.toEntity() })
                Result.Success(budgets)
            } else {
                Result.Error("Failed to fetch budgets")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getStatus(month: Int? = null, year: Int? = null): Result<List<BudgetStatus>> {
        return try {
            val response = api.getStatus(month, year)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.map { it.toDomain() })
            } else {
                Result.Error("Failed to fetch budget status")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun create(
        category: String, limitAmount: Double, currency: String, month: Int, year: Int
    ): Result<Budget> {
        return try {
            val response = api.create(BudgetRequestDto(category, limitAmount, currency, month, year))
            if (response.isSuccessful) {
                val budget = response.body()!!.toDomain()
                dao.insert(budget.toEntity())
                Result.Success(budget)
            } else {
                Result.Error("Failed to create budget")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun delete(id: Int): Result<Unit> {
        return try {
            val response = api.delete(id)
            if (response.isSuccessful) {
                dao.deleteById(id)
                Result.Success(Unit)
            } else {
                Result.Error("Failed to delete budget")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}

// ─── Bill ────────────────────────────────────────────────────────────────────

class BillRepository(
    private val api: BillApi,
    private val dao: BillDao
) {
    val localBills: Flow<List<Bill>> = dao.getAll().map { it.map { e -> e.toDomain() } }

    suspend fun fetchAll(): Result<List<Bill>> {
        return try {
            val response = api.getAll()
            if (response.isSuccessful) {
                val bills = response.body()!!.map { it.toDomain() }
                dao.deleteAll()
                dao.insertAll(bills.map { it.toEntity() })
                Result.Success(bills)
            } else {
                Result.Error("Failed to fetch bills")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getUpcoming(days: Int = 7): Result<List<Bill>> {
        return try {
            val response = api.getUpcoming(days)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.map { it.toDomain() })
            } else {
                Result.Error("Failed to fetch upcoming bills")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun create(
        title: String, amount: Double, currency: String, recurrence: String,
        dueDay: Int, category: String, notes: String?
    ): Result<Bill> {
        return try {
            val response = api.create(BillRequestDto(title, amount, currency, recurrence, dueDay, category, notes))
            if (response.isSuccessful) {
                val bill = response.body()!!.toDomain()
                dao.insert(bill.toEntity())
                Result.Success(bill)
            } else {
                Result.Error("Failed to create bill")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun markPaid(id: Int): Result<Bill> {
        return try {
            val response = api.markPaid(id)
            if (response.isSuccessful) {
                val bill = response.body()!!.toDomain()
                dao.insert(bill.toEntity())
                Result.Success(bill)
            } else {
                Result.Error("Failed to mark bill as paid")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun delete(id: Int): Result<Unit> {
        return try {
            val response = api.delete(id)
            if (response.isSuccessful) {
                dao.deleteById(id)
                Result.Success(Unit)
            } else {
                Result.Error("Failed to delete bill")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}

// ─── Payments ────────────────────────────────────────────────────────────────

class PaymentRepository(
    private val mpesaApi: MpesaApi,
    private val stripeApi: StripeApi
) {
    suspend fun initiateStkPush(
        phone: String, amount: Double, accountRef: String, description: String
    ): Result<String> {
        return try {
            val response = mpesaApi.stkPush(StkPushRequestDto(phone, amount, accountRef, description))
            if (response.isSuccessful) {
                Result.Success(response.body()!!.checkoutRequestId)
            } else {
                Result.Error("STK Push failed. Check phone number and try again.")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMpesaStatus(checkoutRequestId: String): Result<String> {
        return try {
            val response = mpesaApi.getStatus(checkoutRequestId)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.status)
            } else {
                Result.Error("Failed to get payment status")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun createStripePaymentIntent(amount: Double, currency: String): Result<String> {
        return try {
            val response = stripeApi.createPaymentIntent(StripePaymentIntentRequestDto(amount, currency))
            if (response.isSuccessful) {
                Result.Success(response.body()!!.clientSecret)
            } else {
                Result.Error("Failed to create Stripe payment")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
