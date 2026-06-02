package com.fintrack.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBoxScope.menuAnchor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.domain.model.Bill
import com.fintrack.domain.model.Result
import com.fintrack.domain.repository.BillRepository
import com.fintrack.ui.theme.ExpenseRed
import com.fintrack.ui.theme.IncomeGreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// ─── ViewModel ───────────────────────────────────────────────────────────────

data class BillUiState(
    val isLoading: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val error: String? = null,
    val showAddDialog: Boolean = false
)

class BillViewModel(private val repository: BillRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState

    init { fetchBills() }

    fun fetchBills() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.fetchAll()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(bills = result.data, isLoading = false)
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                else -> {}
            }
        }
    }

    fun createBill(title: String, amount: Double, currency: String, recurrence: String, dueDay: Int, category: String, notes: String?) {
        viewModelScope.launch {
            when (val result = repository.create(title, amount, currency, recurrence, dueDay, category, notes)) {
                is Result.Success -> { _uiState.value = _uiState.value.copy(showAddDialog = false); fetchBills() }
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message)
                else -> {}
            }
        }
    }

    fun markPaid(id: Int) {
        viewModelScope.launch { repository.markPaid(id); fetchBills() }
    }

    fun deleteBill(id: Int) {
        viewModelScope.launch { repository.delete(id); fetchBills() }
    }

    fun showAddDialog() { _uiState.value = _uiState.value.copy(showAddDialog = true) }
    fun hideAddDialog() { _uiState.value = _uiState.value.copy(showAddDialog = false) }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    onBack: () -> Unit,
    viewModel: BillViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showAddDialog) {
        AddBillDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { title, amount, currency, recurrence, dueDay, category, notes ->
                viewModel.createBill(title, amount, currency, recurrence, dueDay, category, notes)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bills & Subscriptions") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = { viewModel.fetchBills() }) { Icon(Icons.Default.Refresh, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.bills.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No bills added yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(uiState.bills) { bill ->
                    BillItem(
                        bill = bill,
                        onMarkPaid = { viewModel.markPaid(bill.id) },
                        onDelete = { viewModel.deleteBill(bill.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun BillItem(bill: Bill, onMarkPaid: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bill") },
            text = { Text("Delete '${bill.title}'?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(if (bill.isPaid) IncomeGreen.copy(0.1f) else ExpenseRed.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.Schedule, null,
                    tint = if (bill.isPaid) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bill.title, fontWeight = FontWeight.Medium)
                Text("${bill.recurrence} • Due day ${bill.dueDay}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("Next: ${bill.nextDueDate.take(10)}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${bill.currency} ${String.format("%,.2f", bill.amount)}",
                    fontWeight = FontWeight.Bold, color = ExpenseRed)
                if (!bill.isPaid) {
                    TextButton(onClick = onMarkPaid, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                        Text("Mark Paid", fontSize = 11.sp, color = IncomeGreen)
                    }
                }
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, Int, String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("KES") }
    var recurrence by remember { mutableStateOf("MONTHLY") }
    var dueDay by remember { mutableStateOf("1") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var recurrenceExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val recurrences = listOf("MONTHLY", "WEEKLY", "YEARLY")
    val categories = listOf("Utilities", "Rent", "Subscriptions", "Insurance", "Internet", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Bill Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it },
                        label = { Text("Amount") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = currency, onValueChange = { currency = it },
                        label = { Text("Currency") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                ExposedDropdownMenuBox(expanded = recurrenceExpanded, onExpandedChange = { recurrenceExpanded = it }) {
                    OutlinedTextField(value = recurrence, onValueChange = {}, readOnly = true,
                        label = { Text("Recurrence") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(recurrenceExpanded) },
                        modifier = Modifier.menuAnchor(type, enabled).fillMaxWidth())
                    ExposedDropdownMenu(expanded = recurrenceExpanded, onDismissRequest = { recurrenceExpanded = false }) {
                        recurrences.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { recurrence = it; recurrenceExpanded = false }) }
                    }
                }
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { category = it; categoryExpanded = false }) }
                    }
                }
                OutlinedTextField(value = dueDay, onValueChange = { dueDay = it },
                    label = { Text("Due Day (1-31)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                val day = dueDay.toIntOrNull() ?: 1
                if (title.isNotBlank() && amt > 0 && category.isNotBlank())
                    onConfirm(title, amt, currency, recurrence, day, category, notes.ifBlank { null })
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
