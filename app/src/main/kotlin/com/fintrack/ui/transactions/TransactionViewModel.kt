package com.fintrack.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.domain.model.Result
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null,
    val isSaved: Boolean = false
)

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState

    init {
        // Observe local DB
        viewModelScope.launch {
            repository.localTransactions.collect { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
        fetchTransactions()
    }

    fun fetchTransactions(type: String? = null, category: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.fetchAll(type, category)) {
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                else -> _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun createTransaction(
        title: String, amount: Double, type: String, category: String,
        currency: String, notes: String?, paymentMethod: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSaved = false)
            val date = Instant.now().toString()
            when (val result = repository.create(title, amount, type, category, currency, notes, date, paymentMethod)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                else -> {}
            }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun resetSaved() { _uiState.value = _uiState.value.copy(isSaved = false) }
}
