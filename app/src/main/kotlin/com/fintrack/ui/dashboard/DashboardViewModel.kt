package com.fintrack.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.domain.model.Bill
import com.fintrack.domain.model.BudgetStatus
import com.fintrack.domain.model.Result
import com.fintrack.domain.model.TransactionSummary
import com.fintrack.domain.repository.AuthRepository
import com.fintrack.domain.repository.BillRepository
import com.fintrack.domain.repository.BudgetRepository
import com.fintrack.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val isLoading: Boolean = false,
    val summary: TransactionSummary? = null,
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val upcomingBills: List<Bill> = emptyList(),
    val username: String = "",
    val error: String? = null
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val billRepository: BillRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        val now = LocalDate.now()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Fetch summary
            when (val result = transactionRepository.getSummary(now.monthValue, now.year)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(summary = result.data)
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message)
                else -> {}
            }

            // Fetch budget status
            when (val result = budgetRepository.getStatus(now.monthValue, now.year)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(budgetStatuses = result.data)
                else -> {}
            }

            // Fetch upcoming bills
            when (val result = billRepository.getUpcoming(7)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(upcomingBills = result.data)
                else -> {}
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
