package com.fintrack.ui.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.domain.model.BudgetStatus
import com.fintrack.domain.model.Result
import com.fintrack.domain.repository.BudgetRepository
import com.fintrack.ui.theme.ExpenseRed
import com.fintrack.ui.theme.IncomeGreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

// ─── ViewModel ───────────────────────────────────────────────────────────────

data class BudgetUiState(
    val isLoading: Boolean = false,
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val error: String? = null,
    val isSaved: Boolean = false,
    val showAddDialog: Boolean = false
)

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState

    init { fetchBudgets() }

    fun fetchBudgets() {
        val now = LocalDate.now()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getStatus(now.monthValue, now.year)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(budgetStatuses = result.data, isLoading = false)
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                else -> {}
            }
        }
    }

    fun createBudget(category: String, limitAmount: Double, currency: String) {
        val now = LocalDate.now()
        viewModelScope.launch {
            when (val result = repository.create(category, limitAmount, currency, now.monthValue, now.year)) {
                is Result.Success -> { _uiState.value = _uiState.value.copy(isSaved = true, showAddDialog = false); fetchBudgets() }
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message)
                else -> {}
            }
        }
    }

    fun deleteBudget(id: Int) {
        viewModelScope.launch { repository.delete(id); fetchBudgets() }
    }

    fun showAddDialog() { _uiState.value = _uiState.value.copy(showAddDialog = true) }
    fun hideAddDialog() { _uiState.value = _uiState.value.copy(showAddDialog = false) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val now = LocalDate.now()

    if (uiState.showAddDialog) {
        AddBudgetDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { category, amount, currency ->
                viewModel.createBudget(category, amount, currency)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets — ${now.month.name.take(3)} ${now.year}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = { viewModel.fetchBudgets() }) { Icon(Icons.Default.Refresh, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.budgetStatuses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PieChart, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No budgets set", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    TextButton(onClick = { viewModel.showAddDialog() }) { Text("Add Budget") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(uiState.budgetStatuses) { status ->
                    BudgetStatusDetailCard(status = status, onDelete = { viewModel.deleteBudget(status.budget.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun BudgetStatusDetailCard(status: BudgetStatus, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }
    val overBudget = status.percentage >= 100
    val nearLimit = status.percentage >= 80

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete Budget") },
            text = { Text("Delete the budget for '${status.budget.category}'?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDelete = false }) { Text("Delete", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(status.budget.category, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("${status.budget.currency} budget", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${String.format("%.0f", status.percentage)}%",
                        color = when {
                            overBudget -> ExpenseRed
                            nearLimit -> Color(0xFFF57F17)
                            else -> IncomeGreen
                        },
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (status.percentage / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = when {
                    overBudget -> ExpenseRed
                    nearLimit -> Color(0xFFF57F17)
                    else -> IncomeGreen
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spent: KES ${String.format("%,.2f", status.spent)}", fontSize = 13.sp)
                Text("Limit: KES ${String.format("%,.2f", status.budget.limitAmount)}", fontSize = 13.sp)
            }
            if (overBudget) {
                Spacer(Modifier.height(4.dp))
                Text("⚠ Over budget by KES ${String.format("%,.2f", -status.remaining)}",
                    color = ExpenseRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("KES") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Food", "Transport", "Utilities", "Entertainment", "Healthcare",
        "Shopping", "Education", "Rent", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = category, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { category = it; categoryExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    label = { Text("Limit Amount") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = currency, onValueChange = { currency = it },
                    label = { Text("Currency") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (category.isNotBlank() && amt > 0) onConfirm(category, amt, currency)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
